import React from 'react';

interface Props {
  label: string;
  value: string | number;
  color?: string;
}

export const StatCard = ({ label, value, color = '#1a1a1a' }: Props) => (
  <div style={{
    background: '#fff',
    border: '1px solid #e5e7eb',
    borderRadius: 8,
    padding: '16px 20px',
  }}>
    <p style={{ fontSize: 12, color: '#6b7280', marginBottom: 4 }}>{label}</p>
    <p style={{ fontSize: 24, fontWeight: 600, color }}>{value}</p>
  </div>
);