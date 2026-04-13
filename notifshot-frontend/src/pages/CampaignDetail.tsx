import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { campaignApi } from '../services/api';
import { Campaign } from '../types';
import { StatusBadge } from '../components/common/StatusBadge';
import { StatCard } from '../components/common/StatCard';

export const CampaignDetail = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [campaign, setCampaign] = useState<Campaign | null>(null);
  const [history, setHistory] = useState<any[]>([]);
  const [retrying, setRetrying] = useState(false);

  useEffect(() => {
    if (!id) return;

    const fetch = () => {
      campaignApi.getById(id).then(data => {
        setCampaign(data);
        setHistory(prev => {
          const point = {
            time: new Date().toLocaleTimeString(),
            sent: data.sentCount,
            failed: data.failedCount,
          };
          return [...prev.slice(-19), point];
        });
      });
    };

    fetch();
    const interval = setInterval(fetch, 3000);
    return () => clearInterval(interval);
  }, [id]);

  const handleRetry = async () => {
    if (!id) return;
    setRetrying(true);
    await campaignApi.retryFailures(id);
    setRetrying(false);
  };

  if (!campaign) return (
    <p style={{ padding: 24, color: '#6b7280' }}>Loading...</p>
  );

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 24 }}>
        <button onClick={() => navigate('/')}
          style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#6b7280', fontSize: 14 }}>
          ← Back
        </button>
        <h1 style={{ fontSize: 24, fontWeight: 700 }}>{campaign.name}</h1>
        <StatusBadge status={campaign.status} />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 16, marginBottom: 24 }}>
        <StatCard label="Total Recipients" value={campaign.totalRecipients.toLocaleString()} />
        <StatCard label="Sent" value={campaign.sentCount.toLocaleString()} color="#15803d" />
        <StatCard label="Failed" value={campaign.failedCount.toLocaleString()} color="#b91c1c" />
        <StatCard label="Delivery Rate" value={`${campaign.deliveryRate.toFixed(1)}%`} color="#2563eb" />
      </div>

      <div style={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: 8, padding: 24, marginBottom: 16 }}>
        <h2 style={{ fontSize: 16, fontWeight: 600, marginBottom: 16 }}>Live Delivery Chart</h2>
        <ResponsiveContainer width="100%" height={240}>
          <AreaChart data={history}>
            <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" />
            <XAxis dataKey="time" tick={{ fontSize: 11 }} />
            <YAxis tick={{ fontSize: 11 }} />
            <Tooltip />
            <Area type="monotone" dataKey="sent" stroke="#2563eb" fill="#dbeafe" name="Sent" />
            <Area type="monotone" dataKey="failed" stroke="#ef4444" fill="#fee2e2" name="Failed" />
          </AreaChart>
        </ResponsiveContainer>
      </div>

      <div style={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: 8, padding: 24 }}>
        <h2 style={{ fontSize: 16, fontWeight: 600, marginBottom: 12 }}>Campaign Details</h2>
        <table style={{ width: '100%', fontSize: 14 }}>
          <tbody>
            {[
              ['Channel', campaign.channel],
              ['Message Template', campaign.messageTemplate],
              ['Skipped', campaign.skippedCount],
              ['Created', new Date(campaign.createdAt).toLocaleString()],
            ].map(([label, value]) => (
              <tr key={label} style={{ borderBottom: '1px solid #f3f4f6' }}>
                <td style={{ padding: '10px 0', color: '#6b7280', width: 160 }}>{label}</td>
                <td style={{ padding: '10px 0' }}>{value}</td>
              </tr>
            ))}
          </tbody>
        </table>

        {campaign.failedCount > 0 && (
          <button
            onClick={handleRetry}
            disabled={retrying}
            style={{
              marginTop: 16, padding: '8px 16px',
              background: retrying ? '#f3f4f6' : '#fee2e2',
              color: '#b91c1c', border: '1px solid #fecaca',
              borderRadius: 6, fontSize: 13, fontWeight: 500, cursor: 'pointer',
            }}
          >
            {retrying ? 'Retrying...' : `Retry ${campaign.failedCount} Failed Jobs`}
          </button>
        )}
      </div>
    </div>
  );
};