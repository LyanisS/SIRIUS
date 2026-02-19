interface TimeDisplayProps {
  time: string;
  date: string;
}

export default function TimeDisplay({ time, date }: TimeDisplayProps) {
  return (
    <div className="bg-white rounded-lg shadow-lg p-8 max-w-2xl mx-auto text-center">
      <h2 className="text-2xl font-bold text-gray-800 mb-4">Current Time</h2>
      <p className="text-5xl font-mono text-blue-600 mb-2">{time}</p>
      <p className="text-xl text-gray-500">{date}</p>
    </div>
  );
}
