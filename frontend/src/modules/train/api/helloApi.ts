export interface Motd {
  id: number;
  message: string;
}

export async function getMotd(): Promise<Motd> {
  const response = await fetch('/api/hello/motd');
  if (!response.ok) {
    throw new Error('Failed to fetch MOTD');
  }
  return response.json();
}

export async function updateMotd(message: string): Promise<Motd> {
  const response = await fetch('/api/hello/motd', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ message }),
  });
  if (!response.ok) {
    throw new Error('Failed to update MOTD');
  }
  return response.json();
}
