import { useState } from 'react';

interface MotdFormProps {
  initialMessage: string;
  onSave: (message: string) => void;
  saving: boolean;
}

export default function MotdForm({ initialMessage, onSave, saving }: MotdFormProps) {
  const [message, setMessage] = useState(initialMessage);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSave(message);
  };

  return (
    <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow-lg p-8 max-w-2xl mx-auto">
      <h2 className="text-2xl font-bold text-gray-800 mb-4">Edit Message of the Day</h2>
      <textarea
        value={message}
        onChange={(e) => setMessage(e.target.value)}
        className="w-full p-3 border border-gray-300 rounded-lg mb-4 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        rows={4}
        placeholder="Enter your message..."
      />
      <button
        type="submit"
        disabled={saving}
        className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 disabled:bg-blue-400 transition-colors"
      >
        {saving ? 'Saving...' : 'Save'}
      </button>
    </form>
  );
}
