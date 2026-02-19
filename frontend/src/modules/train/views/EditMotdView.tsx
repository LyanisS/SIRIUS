import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMotd, updateMotd, Motd } from '../api/helloApi';
import MotdForm from '../components/MotdForm';

export default function EditMotdView() {
  const navigate = useNavigate();
  const [motd, setMotd] = useState<Motd | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    getMotd()
      .then(setMotd)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  const handleSave = async (message: string) => {
    setSaving(true);
    try {
      await updateMotd(message);
      navigate('/');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <div className="text-center text-gray-600">Loading...</div>;
  }

  if (error) {
    return <div className="text-center text-red-600">Error: {error}</div>;
  }

  return motd ? (
    <MotdForm initialMessage={motd.message} onSave={handleSave} saving={saving} />
  ) : null;
}
