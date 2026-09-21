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
   * 1. localStorage override (finvisiq_api_url)
   * 2. Local development origin check (localhost / 127.0.0.1 -> http://localhost:8085)
   * 3. window.ENV.API_URL or window.ENV.VITE_API_BASE_URL (injected runtime config)
   * 4. Production Railway URL
   */
  getBaseUrl() {
    const sanitizeUrl = (url) => {
      if (!url) return '';
      let clean = url.trim().replace(/\/+$/, '');
      if (clean.endsWith('/api')) {
        clean = clean.slice(0, -4).replace(/\/+$/, '');
      }
      return clean;
    };

    // 1. Check custom override from user settings modal
    const custom = localStorage.getItem(this.apiUrlKey);
    if (custom && custom.trim() !== '') {
      return sanitizeUrl(custom);
    }

    // 2. Check local development host
    const host = window.location.hostname;
    if (host === 'localhost' || host === '127.0.0.1') {
      return 'http://localhost:8085';
    }

    // 3. Runtime environment variables (Netlify deploy or window.ENV)
    if (window.ENV) {
      const envUrl = window.ENV.API_URL || window.ENV.VITE_API_BASE_URL;
      if (envUrl && envUrl.trim() !== '') {
        return sanitizeUrl(envUrl);
      }
    }

    // 4. Default Production Railway Backend Endpoint
    return 'https://finvisiq-personal-finance-intelligence-platform-production.up.railway.app';
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
    let cleanPath = path.startsWith('/') ? path : '/' + path;
    // Guard against accidental double /api/api path construction
    if (baseUrl.endsWith('/api') && cleanPath.startsWith('/api/')) {
      cleanPath = cleanPath.substring(4);
    }
    const url = `${baseUrl}${cleanPath}`;

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
        // Unauthorized - session expired or invalid credentials
        this.setToken(null);
        this.setUser(null);
        window.dispatchEvent(new CustomEvent('finvisiq:unauthorized', { detail: { path } }));
        const err = new Error(data.message || 'Authentication required: Invalid credentials or session expired (HTTP 401).');
        err.status = 401;
        throw err;
      }

      if (response.status === 403) {
        const err = new Error(data.message || 'Access Forbidden: You do not have permission to access this resource (HTTP 403).');
        err.status = 403;
        throw err;
      }

      if (response.status === 404) {
        const err = new Error(data.message || `Endpoint not found: The requested API path "${cleanPath}" was not found (HTTP 404).`);
        err.status = 404;
        throw err;
      }

      if (response.status >= 502 && response.status <= 504) {
        const err = new Error(data.message || `Backend Service Unavailable (HTTP ${response.status}): The Railway backend is waking up or temporarily unreachable. Please retry in a moment.`);
        err.status = response.status;
        throw err;
      }

      if (response.status >= 500) {
        const err = new Error(data.message || `Internal Server Error (HTTP ${response.status}): The backend encountered an unexpected condition.`);
        err.status = response.status;
        throw err;
      }

      if (!response.ok) {
        const err = new Error(data.message || `API request failed with HTTP status ${response.status}`);
        err.status = response.status;
        throw err;
      }

      return data;
    } catch (error) {
      if (!error.status) {
        // Network or CORS failure: fetch() rejected before an HTTP response was obtained
        const isNetworkOrCors = error.name === 'TypeError' || (error.message && error.message.toLowerCase().includes('fetch'));
        if (isNetworkOrCors) {
          const detailMsg = `Network or CORS Connection Error: Unable to reach FinvisIQ backend at ${baseUrl}. Please check internet connection or verify the Railway server is running and allowing CORS from ${window.location.origin}.`;
          console.error(`[FinvisIQ Network/CORS Error] ${options.method || 'GET'} ${url}:`, error);
          const networkErr = new Error(detailMsg);
          networkErr.status = 0;
          networkErr.isNetworkError = true;
          throw networkErr;
        }
      }
      console.error(`[FinvisIQ API Error] ${options.method || 'GET'} ${cleanPath} [Status: ${error.status || 'ERR'}]:`, error);
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
