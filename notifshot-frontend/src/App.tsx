import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Layout } from './components/layout/Layout';
import { CampaignList } from './pages/CampaignList';
import { CampaignCreate } from './pages/CampaignCreate';
import { CampaignDetail } from './pages/CampaignDetail';
import { TenantList } from './pages/TenantList';

function App() {
  return (
    <BrowserRouter>
      <Layout>
        <Routes>
          <Route path="/" element={<CampaignList />} />
          <Route path="/create" element={<CampaignCreate />} />
          <Route path="/campaigns/:id" element={<CampaignDetail />} />
          <Route path="/tenants" element={<TenantList />} />
        </Routes>
      </Layout>
    </BrowserRouter>
  );
}

export default App;