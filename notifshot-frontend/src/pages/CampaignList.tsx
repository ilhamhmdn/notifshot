import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { campaignApi } from '../services/api';
import { Campaign } from '../types';
import { StatusBadge } from '../components/common/StatusBadge';

export const CampaignList = () => {
  const [campaigns, setCampaigns] = useState<Campaign[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  const fetchCampaigns = () => {
    setLoading(true);
    campaignApi.getAll(undefined, page, 10)
      .then(data => {
        setCampaigns(data.content);
        setTotal(data.totalElements);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchCampaigns();
    const interval = setInterval(fetchCampaigns, 5000);
    return () => clearInterval(interval);
  }, [page]);

  const filtered = campaigns.filter(c =>
    c.name.toLowerCase().includes(search.toLowerCase()) ||
    c.channel.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 24 }}>
        <h1 style={{ fontSize: 24, fontWeight: 700 }}>Campaigns</h1>
        <Link to="/create" style={{
          background: '#2563eb', color: '#fff',
          padding: '8px 16px', borderRadius: 6, fontSize: 14, fontWeight: 500,
        }}>
          + New Campaign
        </Link>
      </div>

      <input
        placeholder="Search campaigns..."
        value={search}
        onChange={e => setSearch(e.target.value)}
        style={{
          width: '100%', padding: '8px 12px', marginBottom: 16,
          border: '1px solid #e5e7eb', borderRadius: 6, fontSize: 14,
        }}
      />

      <div style={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: 8 }}>
        {loading && campaigns.length === 0 ? (
          <p style={{ padding: 24, color: '#6b7280', textAlign: 'center' }}>Loading...</p>
        ) : filtered.length === 0 ? (
          <p style={{ padding: 24, color: '#6b7280', textAlign: 'center' }}>No campaigns found</p>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid #e5e7eb', background: '#f9fafb' }}>
                {['Name', 'Channel', 'Status', 'Recipients', 'Sent', 'Failed', 'Delivery Rate', 'Created'].map(h => (
                  <th key={h} style={{ padding: '12px 16px', textAlign: 'left', fontSize: 12, color: '#6b7280', fontWeight: 600 }}>
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {filtered.map(c => (
                <tr key={c.id} style={{ borderBottom: '1px solid #f3f4f6' }}>
                  <td style={{ padding: '12px 16px' }}>
                    <Link to={`/campaigns/${c.id}`} style={{ color: '#2563eb', fontWeight: 500 }}>
                      {c.name}
                    </Link>
                  </td>
                  <td style={{ padding: '12px 16px', fontSize: 13 }}>{c.channel}</td>
                  <td style={{ padding: '12px 16px' }}><StatusBadge status={c.status} /></td>
                  <td style={{ padding: '12px 16px', fontSize: 13 }}>{c.totalRecipients.toLocaleString()}</td>
                  <td style={{ padding: '12px 16px', fontSize: 13, color: '#15803d' }}>{c.sentCount.toLocaleString()}</td>
                  <td style={{ padding: '12px 16px', fontSize: 13, color: '#b91c1c' }}>{c.failedCount.toLocaleString()}</td>
                  <td style={{ padding: '12px 16px', fontSize: 13 }}>{c.deliveryRate.toFixed(1)}%</td>
                  <td style={{ padding: '12px 16px', fontSize: 12, color: '#6b7280' }}>
                    {new Date(c.createdAt).toLocaleDateString()}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        <div style={{ padding: '12px 16px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderTop: '1px solid #f3f4f6' }}>
          <span style={{ fontSize: 13, color: '#6b7280' }}>{total} total campaigns</span>
          <div style={{ display: 'flex', gap: 8 }}>
            <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0}
              style={{ padding: '4px 12px', border: '1px solid #e5e7eb', borderRadius: 4, background: '#fff', cursor: 'pointer', fontSize: 13 }}>
              Previous
            </button>
            <span style={{ fontSize: 13, padding: '4px 8px' }}>Page {page + 1}</span>
            <button onClick={() => setPage(p => p + 1)} disabled={(page + 1) * 10 >= total}
              style={{ padding: '4px 12px', border: '1px solid #e5e7eb', borderRadius: 4, background: '#fff', cursor: 'pointer', fontSize: 13 }}>
              Next
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};