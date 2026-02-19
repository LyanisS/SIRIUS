interface MotdCardProps {
  message: string;
}

export default function MotdCard({ message }: MotdCardProps) {
  return (
    <div className="bg-white rounded-lg shadow-lg p-8 max-w-2xl mx-auto">
      <h2 className="text-2xl font-bold text-gray-800 mb-4">Message of the Day</h2>
      <p className="text-xl text-gray-600">{message}</p>
    </div>
  );
}
