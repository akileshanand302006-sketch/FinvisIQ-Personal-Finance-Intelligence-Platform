/**
 * FinvisIQ API Client Module
 * Handles dynamic API routing, JWT token management, and REST communications.
 */
class FinvisIQApi {
  constructor() {
    this.tokenKey = 'finvisiq_jwt';
    this.userKey = 'finvisiq_user';
    this.apiUrlKey = 'finvisiq_api_url';
  }

  /**
   * Resolve Backend API Base URL dynamically.
   * Priority:
   * 1. window.ENV.API_URL (injected during deploy)
   * 2. localStorage override (finvisiq_api_url)
   * 3. Local development origin check (localhost:8085)
   * 4. Production Railway URL
   */
  getBaseUrl() {
    if (window.ENV && window.ENV.API_URL && window.ENV.API_URL.trim() !== '') {
      return window.ENV.API_URL.replace(/\/+$/, '');
    }

    const custom = localStorage.getItem(this.apiUrlKey);
    if (custom && custom.trim() !== '') {
      return custom.replace(/\/+$/, '');
    }

    const host = window.location.hostname;
    if (host === 'localhost' || host === '127.0.0.1') {
      return 'http://localhost:8085';
    }

    // Default Railway Backend Endpoint
    return 'https://finvisiq-backend-production.up.railway.app';
  }

  getToken() {
    return localStorage.getItem(this.tokenKey);
  }

  setToken(token) {
    if (token) {
      localStorage.setItem(this.tokenKey, token);
    } else {
      localStorage.removeItem(this.tokenKey);
    }
  }

  getUser() {
    try {
      const u = localStorage.getItem(this.userKey);
      return u ? JSON.parse(u) : null;
    } catch (e) {
      return null;
    }
  }

  setUser(user) {
    if (user) {
      localStorage.setItem(this.userKey, JSON.stringify(user));
    } else {
      localStorage.removeItem(this.userKey);
    }
  }

  isLoggedIn() {
    return !!this.getToken();
  }

  logout() {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    window.dispatchEvent(new CustomEvent('finvisiq:auth-changed', { detail: { loggedIn: false } }));
  }

  async request(path, options = {}) {
    const baseUrl = this.getBaseUrl();
    const url = `${baseUrl}${path.startsWith('/') ? path : '/' + path}`;

    const headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      ...(options.headers || {})
    };

    const token = this.getToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
      ...options,
      headers
    };

    try {
      const response = await fetch(url, config);
      const data = await response.json().catch(() => ({}));

      if (response.status === 401) {
        // Unauthorized
        this.setToken(null);
        this.setUser(null);
        window.dispatchEvent(new CustomEvent('finvisiq:unauthorized', { detail: { path } }));
        throw new Error(data.message || 'Session expired or invalid credentials.');
      }

      if (!response.ok) {
        throw new Error(data.message || `Request failed with status ${response.status}`);
      }

      return data;
    } catch (error) {
      console.error(`[FinvisIQ API Error] ${options.method || 'GET'} ${path}:`, error);
      throw error;
    }
  }

  /* ============================================================================
     ENDPOINTS
     ============================================================================ */

  // Health
  async checkHealth() {
    return this.request('/api/health');
  }

  async checkDbHealth() {
    return this.request('/api/health/db');
  }

  // Auth
  async login(username, password) {
    const res = await this.request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password })
    });
    if (res.success && res.data?.token) {
      this.setToken(res.data.token);
      this.setUser({
        userId: res.data.userId,
        name: res.data.name,
        email: res.data.email,
        role: res.data.role
      });
      window.dispatchEvent(new CustomEvent('finvisiq:auth-changed', { detail: { loggedIn: true, user: res.data } }));
    }
    return res;
  }

  async register(name, email, password) {
    const res = await this.request('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify({ name, email, password })
    });
    if (res.success && res.data?.token) {
      this.setToken(res.data.token);
      this.setUser({
        userId: res.data.userId,
        name: res.data.name,
        email: res.data.email,
        role: res.data.role
      });
      window.dispatchEvent(new CustomEvent('finvisiq:auth-changed', { detail: { loggedIn: true, user: res.data } }));
    }
    return res;
  }

  async getCurrentUser() {
    return this.request('/api/auth/me');
  }

  // Dashboard
  async getDashboard() {
    return this.request('/api/dashboard');
  }

  // Transactions
  async getTransactions() {
    return this.request('/api/transactions');
  }

  async createTransaction(transactionData) {
    return this.request('/api/transactions', {
      method: 'POST',
      body: JSON.stringify(transactionData)
    });
  }

  async deleteTransaction(id) {
    return this.request(`/api/transactions/${id}`, {
      method: 'DELETE'
    });
  }

  // Budgets
  async getBudgets() {
    return this.request('/api/budgets');
  }

  async createBudget(budgetData) {
    return this.request('/api/budgets', {
      method: 'POST',
      body: JSON.stringify(budgetData)
    });
  }

  async deleteBudget(id) {
    return this.request(`/api/budgets/${id}`, {
      method: 'DELETE'
    });
  }

  // Goals
  async getGoals() {
    return this.request('/api/goals');
  }

  async createGoal(goalData) {
    return this.request('/api/goals', {
      method: 'POST',
      body: JSON.stringify(goalData)
    });
  }

  async contributeToGoal(id, amount) {
    return this.request(`/api/goals/${id}/contribute`, {
      method: 'POST',
      body: JSON.stringify({ amount: Number(amount) })
    });
  }

  // Investments
  async getInvestments() {
    return this.request('/api/investments');
  }

  async createInvestment(investmentData) {
    return this.request('/api/investments', {
      method: 'POST',
      body: JSON.stringify(investmentData)
    });
  }

  async calculateSIP(monthlyInvestment, expectedReturnRate, timePeriodYears) {
    return this.request('/api/investments/sip-calculator', {
      method: 'POST',
      body: JSON.stringify({
        monthlyInvestment: Number(monthlyInvestment),
        expectedReturnRate: Number(expectedReturnRate),
        timePeriodYears: Number(timePeriodYears)
      })
    });
  }

  // Net Worth & Assets
  async getNetWorth() {
    return this.request('/api/networth');
  }

  async getAssets() {
    return this.request('/api/assets');
  }

  async createAsset(assetData) {
    return this.request('/api/assets', {
      method: 'POST',
      body: JSON.stringify(assetData)
    });
  }

  async getLiabilities() {
    return this.request('/api/liabilities');
  }

  async createLiability(liabilityData) {
    return this.request('/api/liabilities', {
      method: 'POST',
      body: JSON.stringify(liabilityData)
    });
  }

  // Subscriptions
  async getSubscriptions() {
    return this.request('/api/subscriptions');
  }

  async createSubscription(subscriptionData) {
    return this.request('/api/subscriptions', {
      method: 'POST',
      body: JSON.stringify(subscriptionData)
    });
  }

  async deleteSubscription(id) {
    return this.request(`/api/subscriptions/${id}`, {
      method: 'DELETE'
    });
  }

  // AI Financial Advisor Insights
  async getAiInsights() {
    return this.request('/api/ai/insights');
  }
}

// Global singleton instance
window.api = new FinvisIQApi();
