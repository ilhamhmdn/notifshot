import React from 'react';
import { CampaignStatus } from '../../types';

const styles: Record<CampaignStatus, React.CSSProperties> = {
  RUNNING:   { background: '#dbeafe', color: '#1d4ed8' },
  COMPLETED: { background: '#dcfce7', color: '#15803d' },
  FAILED:    { background: '#fee2e2', color: '#b91c1c' },
  SCHEDULED: { background: '#fef9c3', color: '#854d0e' },
  PENDING:   { background: '#f3f4f6', color: '#374151' },
};

export const StatusBadge = ({ status }: { status: CampaignStatus }) => (
  <span style={{
    ...styles[status],
    padding: '2px 10px',
    borderRadius: 12,
    fontSize: 12,
    fontWeight: 500,
  }}>
    {status}
  </span>
);