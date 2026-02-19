import { useEffect, useState } from 'react';
import { getMotd, Motd } from '../api/helloApi';
import MotdCard from '../components/MotdCard';

export default function HelloView() {
  const [motd, setMotd] = useState<Motd | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    getMotd()
      .then(setMotd)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return <div className="text-center text-gray-600">Loading...</div>;
  }

  if (error) {
    return <div className="text-center text-red-600">Error: {error}</div>;
  }

  return motd ? <MotdCard message={motd.message} /> : null;
}
