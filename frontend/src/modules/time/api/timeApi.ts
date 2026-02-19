export interface TimeData {
  time: string;
  date: string;
  datetime: string;
}

export async function getCurrentTime(): Promise<TimeData> {
  const response = await fetch('/api/time');
  if (!response.ok) {
    throw new Error('Failed to fetch time');
  }
  return response.json();
}
