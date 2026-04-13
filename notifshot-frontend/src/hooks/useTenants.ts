import { useState, useEffect } from 'react';
import { tenantApi } from '../services/api';

export const useTenants = () => {
  const [tenants, setTenants] = useState<any[]>([]);
  const [tenantMap, setTenantMap] = useState<Record<string, string>>({});

  useEffect(() => {
    tenantApi.getAll().then(data => {
      setTenants(data);
      const map: Record<string, string> = {};
      data.forEach((t: any) => { map[t.id] = t.name; });
      setTenantMap(map);
    });
  }, []);

  return { tenants, tenantMap };
};