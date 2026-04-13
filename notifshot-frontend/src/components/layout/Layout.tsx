import React from 'react';
import { Link, useLocation } from 'react-router-dom';

export const Layout = ({ children }: { children: React.ReactNode }) => {
  const location = useLocation();

  const navItem = (to: string, label: string) => (
    <Link to={to} style={{
      padding: '8px 16px',
      borderRadius: 6,
      fontWeight: 500,
      fontSize: 14,
      color: location.pathname === to ? '#2563eb' : '#6b7280',
      background: location.pathname === to ? '#eff6ff' : 'transparent',
    }}>
      {label}
    </Link>
  );

  return (
    <div style={{ minHeight: '100vh', background: '#f9fafb' }}>
      <nav style={{
        background: '#fff',
        borderBottom: '1px solid #e5e7eb',
        padding: '0 32px',
        display: 'flex',
        alignItems: 'center',
        gap: 8,
        height: 56,
      }}>
        <span style={{ fontWeight: 700, fontSize: 18, marginRight: 32 }}>
          Notifshot
        </span>
        {navItem('/', 'Campaigns')}
        {navItem('/create', 'Create Campaign')}
      </nav>
      <main style={{ maxWidth: 1200, margin: '0 auto', padding: '32px 24px' }}>
        {children}
      </main>
    </div>
  );
};