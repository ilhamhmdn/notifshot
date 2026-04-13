import React, { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { campaignApi } from '../services/api';
import { Channel } from '../types';

export const CampaignCreate = () => {
  const navigate = useNavigate();
  const fileRef = useRef<HTMLInputElement>(null);
  const [file, setFile] = useState<File | null>(null);
  const [preview, setPreview] = useState<string[][]>([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const [form, setForm] = useState({
    tenantId: '',
    name: '',
    channel: 'EMAIL' as Channel,
    messageTemplate: '',
    transactional: false,
  });

  const handleFile = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selected = e.target.files?.[0];
    if (!selected) return;

    if (!selected.name.endsWith('.csv')) {
      setError('File must be a CSV');
      return;
    }

    setFile(selected);
    setError('');

    const reader = new FileReader();
    reader.onload = (ev) => {
      const text = ev.target?.result as string;
      const rows = text.split('\n').slice(0, 6).map(r => r.split(','));
      setPreview(rows);
    };
    reader.readAsText(selected);
  };

  const handleSubmit = async () => {
    if (!form.tenantId || !form.name || !form.messageTemplate) {
      setError('Please fill in all required fields');
      return;
    }
    if (!file) {
      setError('Please upload a CSV file');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const campaign = await campaignApi.create(form, file);
      navigate(`/campaigns/${campaign.id}`);
    } catch (e: any) {
      setError(e.response?.data?.error || 'Failed to create campaign');
    } finally {
      setLoading(false);
    }
  };

  const input = (label: string, field: keyof typeof form, type = 'text') => (
    <div style={{ marginBottom: 16 }}>
      <label style={{ display: 'block', fontSize: 13, fontWeight: 500, marginBottom: 4, color: '#374151' }}>
        {label} <span style={{ color: '#ef4444' }}>*</span>
      </label>
      <input
        type={type}
        value={form[field] as string}
        onChange={e => setForm(f => ({ ...f, [field]: e.target.value }))}
        style={{ width: '100%', padding: '8px 12px', border: '1px solid #e5e7eb', borderRadius: 6, fontSize: 14 }}
      />
    </div>
  );

  return (
    <div style={{ maxWidth: 640 }}>
      <h1 style={{ fontSize: 24, fontWeight: 700, marginBottom: 24 }}>Create Campaign</h1>

      <div style={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: 8, padding: 24 }}>
        {input('Tenant ID', 'tenantId')}
        {input('Campaign Name', 'name')}

        <div style={{ marginBottom: 16 }}>
          <label style={{ display: 'block', fontSize: 13, fontWeight: 500, marginBottom: 4, color: '#374151' }}>
            Channel <span style={{ color: '#ef4444' }}>*</span>
          </label>
          <select
            value={form.channel}
            onChange={e => setForm(f => ({ ...f, channel: e.target.value as Channel }))}
            style={{ width: '100%', padding: '8px 12px', border: '1px solid #e5e7eb', borderRadius: 6, fontSize: 14 }}
          >
            <option>EMAIL</option>
            <option>SMS</option>
            <option>PUSH</option>
          </select>
        </div>

        <div style={{ marginBottom: 16 }}>
          <label style={{ display: 'block', fontSize: 13, fontWeight: 500, marginBottom: 4, color: '#374151' }}>
            Message Template <span style={{ color: '#ef4444' }}>*</span>
          </label>
          <textarea
            value={form.messageTemplate}
            onChange={e => setForm(f => ({ ...f, messageTemplate: e.target.value }))}
            rows={4}
            style={{ width: '100%', padding: '8px 12px', border: '1px solid #e5e7eb', borderRadius: 6, fontSize: 14, resize: 'vertical' }}
          />
        </div>

        <div style={{ marginBottom: 16, display: 'flex', alignItems: 'center', gap: 8 }}>
          <input
            type="checkbox"
            checked={form.transactional}
            onChange={e => setForm(f => ({ ...f, transactional: e.target.checked }))}
            id="transactional"
          />
          <label htmlFor="transactional" style={{ fontSize: 13, color: '#374151' }}>
            Transactional (bypasses quiet hours)
          </label>
        </div>

        <div style={{ marginBottom: 16 }}>
          <label style={{ display: 'block', fontSize: 13, fontWeight: 500, marginBottom: 4, color: '#374151' }}>
            Recipients CSV <span style={{ color: '#ef4444' }}>*</span>
          </label>
          <div
            onClick={() => fileRef.current?.click()}
            style={{
              border: '2px dashed #e5e7eb', borderRadius: 6, padding: '24px',
              textAlign: 'center', cursor: 'pointer', background: '#f9fafb',
            }}
          >
            {file ? (
              <p style={{ color: '#2563eb', fontSize: 14 }}>✓ {file.name} ({(file.size / 1024).toFixed(1)} KB)</p>
            ) : (
              <p style={{ color: '#6b7280', fontSize: 14 }}>Click to upload CSV file</p>
            )}
          </div>
          <input ref={fileRef} type="file" accept=".csv" onChange={handleFile} style={{ display: 'none' }} />
        </div>

        {preview.length > 0 && (
          <div style={{ marginBottom: 16 }}>
            <p style={{ fontSize: 12, fontWeight: 500, color: '#374151', marginBottom: 8 }}>Preview (first 5 rows)</p>
            <div style={{ overflow: 'auto', border: '1px solid #e5e7eb', borderRadius: 6 }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 12 }}>
                <tbody>
                  {preview.map((row, i) => (
                    <tr key={i} style={{ background: i === 0 ? '#f9fafb' : '#fff', borderBottom: '1px solid #f3f4f6' }}>
                      {row.map((cell, j) => (
                        <td key={j} style={{ padding: '6px 10px', fontWeight: i === 0 ? 600 : 400 }}>{cell}</td>
                      ))}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {error && (
          <div style={{ background: '#fee2e2', color: '#b91c1c', padding: '8px 12px', borderRadius: 6, fontSize: 13, marginBottom: 16 }}>
            {error}
          </div>
        )}

        <button
          onClick={handleSubmit}
          disabled={loading}
          style={{
            width: '100%', padding: '10px', background: loading ? '#93c5fd' : '#2563eb',
            color: '#fff', border: 'none', borderRadius: 6, fontSize: 14,
            fontWeight: 500, cursor: loading ? 'not-allowed' : 'pointer',
          }}
        >
          {loading ? 'Creating...' : 'Create Campaign'}
        </button>
      </div>
    </div>
  );
};