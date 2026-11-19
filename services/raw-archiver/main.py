"""raw-archiver : enregistre chaque message du bus RabbitMQ (échange amq.topic, où arrive aussi MQTT) dans le bucket S3 « raw »."""
import os
import signal
import ssl
import sys
import uuid
from datetime import datetime, timezone
from urllib.parse import quote

import boto3
import pika
from botocore.config import Config

E = os.environ
CA = E.get("CA_FILE", "/etc/pcc-ca/ca.crt")
BUCKET = E.get("BUCKET", "raw")
QUEUE = E.get("QUEUE", "raw-archiver")
EXCHANGE = E.get("EXCHANGE", "amq.topic")
BINDING_KEY = E.get("BINDING_KEY", "#")


def object_key(routing_key, now):
    """trains.12.position (MQTT : trains/12/position) -> trains/12/position/2026/11/18/143501123456-1a2b3c4d"""
    path = "/".join(p for p in routing_key.split(".") if p) or "_"
    return f"{path}/{now:%Y/%m/%d/%H%M%S%f}-{uuid.uuid4().hex[:8]}"


def main():
    s3 = boto3.client(
        "s3",
        endpoint_url=E["S3_ENDPOINT"],
        region_name=E.get("S3_REGION", "garage"),
        aws_access_key_id=E["S3_KEY_ID"],
        aws_secret_access_key=E["S3_SECRET"],
        verify=CA,
        config=Config(s3={"addressing_style": "virtual"}),  # https://<bucket>.datalake.pcc.internal
    )

    def on_message(ch, method, props, body):
        s3.put_object(
            Bucket=BUCKET,
            Key=object_key(method.routing_key, datetime.now(timezone.utc)),
            Body=body,
            ContentType=props.content_type or "application/octet-stream",
            Metadata={"exchange": method.exchange, "routing-key": quote(method.routing_key)},
        )
        # Acquitté seulement une fois écrit : en cas d'erreur le processus s'arrête et RabbitMQ redonne le message
        ch.basic_ack(method.delivery_tag)

    host = E.get("AMQP_HOST", "data.pcc.internal")
    conn = pika.BlockingConnection(pika.ConnectionParameters(
        host=host,
        port=int(E.get("AMQP_PORT", "5671")),
        credentials=pika.PlainCredentials(E["AMQP_USER"], E["AMQP_PASSWORD"]),
        ssl_options=pika.SSLOptions(ssl.create_default_context(cafile=CA), host),
        heartbeat=30,
    ))
    ch = conn.channel()
    ch.queue_declare(QUEUE, durable=True, arguments={"x-queue-type": "quorum"})
    ch.queue_bind(QUEUE, EXCHANGE, BINDING_KEY)
    # shortcut: un objet S3 par message (~100 messages/s) ; regrouper par lots si le débit du bus dépasse
    ch.basic_qos(prefetch_count=20)
    ch.basic_consume(QUEUE, on_message)
    # PID 1 du conteneur : sans gestionnaire, SIGTERM est ignoré et Kubernetes attend 30 s avant de tuer le pod
    signal.signal(signal.SIGTERM, lambda *_: sys.exit(0))
    print(f"{EXCHANGE} ({BINDING_KEY}) -> file {QUEUE} -> s3://{BUCKET}", flush=True)
    ch.start_consuming()


if __name__ == "__main__":
    main()
