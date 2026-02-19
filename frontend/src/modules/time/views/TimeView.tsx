import { useEffect, useState } from 'react';
import { getCurrentTime, TimeData } from '../api/timeApi';
import TimeDisplay from '../components/TimeDisplay';

export default function TimeView() {
  const [timeData, setTimeData] = useState<TimeData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchTime = () => {
    getCurrentTime()
      .then(setTimeData)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchTime();
    const interval = setInterval(fetchTime, 1000);
    return () => clearInterval(interval);
  }, []);

  if (loading) {
    return <div className="text-center text-gray-600">Loading...</div>;
  }

  if (error) {
    return <div className="text-center text-red-600">Error: {error}</div>;
  }

  return timeData ? <TimeDisplay time={timeData.time} date={timeData.date} /> : null;
}
