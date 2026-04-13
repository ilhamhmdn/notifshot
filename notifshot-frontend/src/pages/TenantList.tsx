import React, { useState, useEffect } from 'react';
import { tenantApi } from '../services/api';

export const TenantList = () => {
  const [tenants, setTenants] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState({ name: '', email: '' });
  const [error, setError] = useState('');
  const [creating, setCreating] = useState(false);
  const [copied, setCopied] = useState<string | null>(null);

  const fetchTenants = () => {
    tenantApi.getAll().then(setTenants).finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchTenants();
  }, []);

  const handleCreate = async () => {
    if (!form.name || !form.email) {
      setError('Name and email are required');
      return;
    }
    setCreating(true);
    setError('');
    try {
      await tenantApi.create(form);
      setShowModal(false);
      setForm({ name: '', email: '' });
      fetchTenants();
    } catch (e: any) {
      setError(e.response?.data?.message || 'Failed to create tenant');
    } finally {
      setCreating(false);
    }
  };

  const copyId = (id: string) => {
    navigator.clipboard.writeText(id);
    setCopied(id);
    setTimeout(() => setCopied(null), 2000);
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 24 }}>
        <h1 style={{ fontSize: 24, fontWeight: 700 }}>Tenants</h1>
        <button
          onClick={() => setShowModal(true)}
          style={{
            background: '#2563eb', color: '#fff',
            padding: '8px 16px', borderRadius: 6, fontSize: 14,
            fontWeight: 500, border: 'none', cursor: 'pointer',
          }}
        >
          + New Tenant
        </button>
      </div>

      <div style={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: 8 }}>
        {loading ? (
          <p style={{ padding: 24, color: '#6b7280', textAlign: 'center' }}>Loading...</p>
        ) : tenants.length === 0 ? (
          <p style={{ padding: 24, color: '#6b7280', textAlign: 'center' }}>No tenants yet</p>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid #e5e7eb', background: '#f9fafb' }}>
                {['Name', 'Email', 'Tenant ID', 'Campaigns Used', 'Messages Used', 'Status'].map(h => (
                  <th key={h} style={{ padding: '12px 16px', textAlign: 'left', fontSize: 12, color: '#6b7280', fontWeight: 600 }}>
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {tenants.map(t => (
                <tr key={t.id} style={{ borderBottom: '1px solid #f3f4f6' }}>
                  <td style={{ padding: '12px 16px', fontWeight: 500 }}>{t.name}</td>
                  <td style={{ padding: '12px 16px', fontSize: 13, color: '#6b7280' }}>{t.email}</td>
                  <td style={{ padding: '12px 16px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                      <code style={{ fontSize: 11, background: '#f3f4f6', padding: '2px 6px', borderRadius: 4 }}>
                        {t.id.substring(0, 8)}...
                      </code>
                      <button
                        onClick={() => copyId(t.id)}
                        style={{
                          fontSize: 11, padding: '2px 8px', border: '1px solid #e5e7eb',
                          borderRadius: 4, background: copied === t.id ? '#dcfce7' : '#fff',
                          color: copied === t.id ? '#15803d' : '#6b7280', cursor: 'pointer',
                        }}
                      >
                        {copied === t.id ? '✓ Copied' : 'Copy ID'}
                      </button>
                    </div>
                  </td>
                  <td style={{ padding: '12px 16px', fontSize: 13 }}>
                    {t.campaignsUsed} / {t.monthlyCampaignLimit}
                  </td>
                  <td style={{ padding: '12px 16px', fontSize: 13 }}>
                    {t.messagesUsed.toLocaleString()} / {t.monthlyMessageLimit.toLocaleString()}
                  </td>
                  <td style={{ padding: '12px 16px' }}>
                    <span style={{
                      padding: '2px 10px', borderRadius: 12, fontSize: 12, fontWeight: 500,
                      background: t.active ? '#dcfce7' : '#fee2e2',
                      color: t.active ? '#15803d' : '#b91c1c',
                    }}>
                      {t.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {showModal && (
        <div style={{
          position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)',
          display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 50,
        }}>
          <div style={{ background: '#fff', borderRadius: 8, padding: 24, width: 440 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 20 }}>
              <h2 style={{ fontSize: 18, fontWeight: 600 }}>Create Tenant</h2>
              <button onClick={() => { setShowModal(false); setError(''); }}
                style={{ background: 'none', border: 'none', fontSize: 18, cursor: 'pointer', color: '#6b7280' }}>
                ×
              </button>
            </div>

            <div style={{ marginBottom: 16 }}>
              <label style={{ display: 'block', fontSize: 13, fontWeight: 500, marginBottom: 4, color: '#374151' }}>
                Company Name <span style={{ color: '#ef4444' }}>*</span>
              </label>
              <input
                value={form.name}
                onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
                placeholder="e.g. Acme Corporation"
                style={{ width: '100%', padding: '8px 12px', border: '1px solid #e5e7eb', borderRadius: 6, fontSize: 14 }}
              />
            </div>

            <div style={{ marginBottom: 16 }}>
              <label style={{ display: 'block', fontSize: 13, fontWeight: 500, marginBottom: 4, color: '#374151' }}>
                Email <span style={{ color: '#ef4444' }}>*</span>
              </label>
              <input
                type="email"
                value={form.email}
                onChange={e => setForm(f => ({ ...f, email: e.target.value }))}
                placeholder="admin@company.com"
                style={{ width: '100%', padding: '8px 12px', border: '1px solid #e5e7eb', borderRadius: 6, fontSize: 14 }}
              />
            </div>

            <p style={{ fontSize: 12, color: '#6b7280', marginBottom: 16 }}>
              Default limits: 100 campaigns/month, 1,000,000 messages/month
            </p>

            {error && (
              <div style={{ background: '#fee2e2', color: '#b91c1c', padding: '8px 12px', borderRadius: 6, fontSize: 13, marginBottom: 16 }}>
                {error}
              </div>
            )}

            <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
              <button
                onClick={() => { setShowModal(false); setError(''); }}
                style={{ padding: '8px 16px', border: '1px solid #e5e7eb', borderRadius: 6, background: '#fff', cursor: 'pointer', fontSize: 14 }}
              >
                Cancel
              </button>
              <button
                onClick={handleCreate}
                disabled={creating}
                style={{
                  padding: '8px 16px', background: creating ? '#93c5fd' : '#2563eb',
                  color: '#fff', border: 'none', borderRadius: 6,
                  cursor: creating ? 'not-allowed' : 'pointer', fontSize: 14, fontWeight: 500,
                }}
              >
                {creating ? 'Creating...' : 'Create Tenant'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};