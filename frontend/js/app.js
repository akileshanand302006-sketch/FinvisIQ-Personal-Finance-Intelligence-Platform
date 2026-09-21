/**
 * FinvisIQ Application Controller
 * Handles SPA navigation, dual themes, Chart.js integrations, count-up animations, and reactive data flow.
 * True Apple/iOS-Inspired Liquid Glass Design System.
 */

// Application State Store
const state = {
  activeView: 'dashboard',
  theme: 'dark',
  currentUser: null,
  dashboardData: null,
  transactions: [],
  filteredTransactions: [],
  budgets: [],
  goals: [],
  investments: [],
  subscriptions: [],
  networth: null,
  aiInsights: null,
  charts: {},
  hasAnimatedNumbers: false
};

// Defensive Currency Formatter (Indian Rupee format)
function formatCurrency(amount) {
  if (amount === undefined || amount === null || isNaN(amount)) return '₹0';
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 0
  }).format(amount);
}

// Defensive Safe String Formatter
function formatSafeText(val, fallback = '—') {
  if (val === undefined || val === null || val === 'undefined' || val === 'null' || val === '') return fallback;
  return val;
}

// ------------------------------------------------------------------------------
// THEME MANAGEMENT (PERSISTENCE & SYSTEM PREFERENCE)
// ------------------------------------------------------------------------------
function initTheme() {
  const saved = localStorage.getItem('finvisiq_theme');
  if (saved) {
    state.theme = saved;
  } else if (window.matchMedia && window.matchMedia('(prefers-color-scheme: light)').matches) {
    state.theme = 'light';
  } else {
    state.theme = 'dark';
  }
  applyTheme(state.theme);
}

function toggleTheme() {
  state.theme = state.theme === 'dark' ? 'light' : 'dark';
  localStorage.setItem('finvisiq_theme', state.theme);
  applyTheme(state.theme);
  refreshAllCharts();
  showToast(`Switched to ${state.theme === 'dark' ? 'Dark' : 'Light'} Mode`, 'info');
}

function applyTheme(theme) {
  document.documentElement.setAttribute('data-theme', theme);
  const btn = document.getElementById('theme-toggle-btn');
  if (btn) {
    btn.setAttribute('title', `Switch to ${theme === 'dark' ? 'Light' : 'Dark'} Mode`);
    btn.setAttribute('aria-label', `Switch to ${theme === 'dark' ? 'Light' : 'Dark'} Mode`);
  }
}

// ------------------------------------------------------------------------------
// FINANCIAL NUMBER COUNT-UP ANIMATION
// ------------------------------------------------------------------------------
function animateNumber(element, start, end, duration = 1000, isCurrency = true, isPercentage = false) {
  if (!element) return;
  const safeEnd = isNaN(end) || end === null || end === undefined ? 0 : Number(end);
  const safeStart = isNaN(start) || start === null || start === undefined ? 0 : Number(start);

  // Respect reduced motion
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    element.innerText = isCurrency ? formatCurrency(safeEnd) : (isPercentage ? `${safeEnd}%` : safeEnd);
    return;
  }

  const startTime = performance.now();
  const diff = safeEnd - safeStart;

  function update(currentTime) {
    const elapsed = currentTime - startTime;
    const progress = Math.min(elapsed / duration, 1);
    // Ease Out Cubic
    const easeProgress = 1 - Math.pow(1 - progress, 3);
    const currentVal = safeStart + diff * easeProgress;

    if (isCurrency) {
      element.innerText = formatCurrency(Math.round(currentVal));
    } else if (isPercentage) {
      element.innerText = `${Math.round(currentVal * 10) / 10}%`;
    } else {
      element.innerText = Math.round(currentVal);
    }

    if (progress < 1) {
      requestAnimationFrame(update);
    } else {
      element.innerText = isCurrency ? formatCurrency(safeEnd) : (isPercentage ? `${safeEnd}%` : safeEnd);
    }
  }

  requestAnimationFrame(update);
}

// ------------------------------------------------------------------------------
// TOAST NOTIFICATION STACK (WITH CRISP SVGS)
// ------------------------------------------------------------------------------
function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;

  const iconSvg = type === 'success'
    ? '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>'
    : type === 'error'
    ? '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>'
    : '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>';

  toast.innerHTML = `<span style="display: inline-flex; align-items: center; justify-content: center; flex-shrink: 0;">${iconSvg}</span><span>${message}</span>`;

  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(50px)';
    setTimeout(() => toast.remove(), 320);
  }, 3800);
}

// ------------------------------------------------------------------------------
// GLOBAL MODAL HELPERS
// ------------------------------------------------------------------------------
function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.add('open');
    const firstInput = modal.querySelector('input:not([type="hidden"]), select');
    if (firstInput) setTimeout(() => firstInput.focus(), 80);
  }
}

function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.remove('open');
  }
}

// Close modals when clicking on the backdrop
document.addEventListener('click', (e) => {
  if (e.target.classList.contains('modal-backdrop')) {
    e.target.classList.remove('open');
  }
});

// ------------------------------------------------------------------------------
// VIEW ROUTING WITH LIQUID TRANSITIONS
// ------------------------------------------------------------------------------
function switchView(viewName) {
  state.activeView = viewName;

  // Update Desktop Sidebar Links
  document.querySelectorAll('.nav-item').forEach(el => {
    if (el.dataset.view === viewName) {
      el.classList.add('active');
    } else {
      el.classList.remove('active');
    }
  });

  // Update Mobile Bottom Nav
  document.querySelectorAll('.mobile-nav-item').forEach(el => {
    if (el.dataset.view === viewName) {
      el.classList.add('active');
    } else {
      el.classList.remove('active');
    }
  });

  // Update View Sections with fade-in animation
  document.querySelectorAll('.view-section').forEach(el => {
    const isTarget = el.id === `view-${viewName}`;
    el.style.display = isTarget ? 'block' : 'none';
    if (isTarget) {
      el.classList.add('active');
    } else {
      el.classList.remove('active');
    }
  });

  // Update Page Title
  const titleMap = {
    dashboard: 'Financial Dashboard',
    transactions: 'Transactions Ledger',
    budgets: 'Category Budgets',
    goals: 'Savings Goals',
    investments: 'Investment Portfolio & SIP',
    networth: 'Net Worth & Liabilities',
    subscriptions: 'Recurring Subscriptions',
    'ai-advisor': 'AI Intelligence Advisor'
  };
  const titleEl = document.getElementById('page-header-title');
  if (titleEl) titleEl.innerText = titleMap[viewName] || 'Dashboard';

  // Load Active View Data
  loadViewData(viewName);
}

async function loadViewData(viewName) {
  if (!window.api.isLoggedIn()) {
    showAuthContainer(true);
    return;
  }

  try {
    switch (viewName) {
      case 'dashboard':
        await loadDashboard();
        break;
      case 'transactions':
        await loadTransactions();
        break;
      case 'budgets':
        await loadBudgets();
        break;
      case 'goals':
        await loadGoals();
        break;
      case 'investments':
        await loadInvestments();
        break;
      case 'networth':
        await loadNetWorth();
        break;
      case 'subscriptions':
        await loadSubscriptions();
        break;
      case 'ai-advisor':
        await loadAiInsights();
        break;
      default:
        await loadDashboard();
    }
  } catch (err) {
    console.error(`Error loading view [${viewName}]:`, err);
    showToast(`Failed to load ${viewName}: ${err.message}`, 'error');
  }
}

// ------------------------------------------------------------------------------
// 1. DASHBOARD VIEW (TELEMETRY, METRICS, CHARTS)
// ------------------------------------------------------------------------------
async function loadDashboard() {
  const res = await window.api.getDashboardSummary();
  if (!res || !res.success || !res.data) return;

  const d = res.data;
  state.dashboardData = d;

  // Time-aware greeting
  const hours = new Date().getHours();
  const timeGreeting = hours < 12 ? 'Good morning' : (hours < 18 ? 'Good afternoon' : 'Good evening');
  const greetingEl = document.getElementById('dash-greeting-title');
  if (greetingEl) {
    const firstName = formatSafeText((d.userName || 'FinvisIQ User').split(' ')[0], 'FinvisIQ User');
    greetingEl.innerText = `${timeGreeting}, ${firstName}`;
  }

  // Live Date Pill
  const datePill = document.getElementById('dash-live-date');
  if (datePill) {
    const options = { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' };
    datePill.innerText = new Date().toLocaleDateString('en-US', options);
  }

  // Defensive Metrics
  const totalIncome = Number(d.totalIncome) || 0;
  const totalExpenses = Number(d.totalExpenses) || 0;
  const netWorth = Number(d.netWorth) || 0;
  const savingsRate = Number(d.savingsRate) || 0;

  // Count-Up Numbers
  const incomeEl = document.getElementById('dash-income');
  const expenseEl = document.getElementById('dash-expense');
  const networthEl = document.getElementById('dash-networth');
  const savingsRateEl = document.getElementById('dash-savings-rate');

  if (!state.hasAnimatedNumbers) {
    animateNumber(incomeEl, 0, totalIncome, 1000, true);
    animateNumber(expenseEl, 0, totalExpenses, 1000, true);
    animateNumber(networthEl, 0, netWorth, 1200, true);
    animateNumber(savingsRateEl, 0, savingsRate, 800, false, true);
    state.hasAnimatedNumbers = true;
  } else {
    if (incomeEl) incomeEl.innerText = formatCurrency(totalIncome);
    if (expenseEl) expenseEl.innerText = formatCurrency(totalExpenses);
    if (networthEl) networthEl.innerText = formatCurrency(netWorth);
    if (savingsRateEl) savingsRateEl.innerText = `${savingsRate}%`;
  }

  // Header Health Score Pill - Defensive against undefined/NaN
  const healthScoreEl = document.getElementById('header-health-score');
  if (healthScoreEl) {
    const rawScore = d.healthScore ?? d.financialHealthScore;
    const score = (rawScore !== undefined && rawScore !== null && !isNaN(rawScore)) ? Math.round(rawScore) : 75;
    healthScoreEl.innerText = `${score}/100`;
  }

  // Render Charts
  renderCategoryDonutChart(d.categoryExpenses);
  renderIncomeExpenseBarChart(totalIncome, totalExpenses);

  // Render Recent Transactions
  renderRecentTransactions(d.recentTransactions);
}

function renderIncomeExpenseBarChart(income, expense) {
  const ctx = document.getElementById('chart-income-expense');
  if (!ctx) return;

  if (state.charts.incomeExpense) {
    state.charts.incomeExpense.destroy();
  }

  const isDark = state.theme === 'dark';
  const gridColor = isDark ? 'rgba(255, 255, 255, 0.06)' : 'rgba(15, 23, 42, 0.06)';
  const textColor = isDark ? '#94a3b8' : '#475569';

  state.charts.incomeExpense = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: ['Total Income', 'Total Expenses', 'Net Balance'],
      datasets: [{
        label: 'Amount (₹)',
        data: [income, expense, Math.max(0, income - expense)],
        backgroundColor: [
          'rgba(16, 185, 129, 0.75)',
          'rgba(244, 63, 94, 0.75)',
          'rgba(56, 189, 248, 0.75)'
        ],
        borderColor: [
          '#10b981',
          '#f43f5e',
          '#38bdf8'
        ],
        borderWidth: 1.5,
        borderRadius: 8
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: isDark ? 'rgba(14, 18, 38, 0.95)' : 'rgba(255, 255, 255, 0.98)',
          titleColor: isDark ? '#fff' : '#0f172a',
          bodyColor: isDark ? '#cbd5e1' : '#334155',
          borderColor: isDark ? 'rgba(139, 92, 246, 0.3)' : 'rgba(139, 92, 246, 0.2)',
          borderWidth: 1,
          padding: 12,
          callbacks: {
            label: (ctx) => ` ${formatCurrency(ctx.raw)}`
          }
        }
      },
      scales: {
        x: {
          grid: { display: false },
          ticks: { color: textColor, font: { family: 'Inter', size: 12 } }
        },
        y: {
          grid: { color: gridColor },
          ticks: {
            color: textColor,
            font: { family: 'Inter', size: 11 },
            callback: (val) => `₹${val >= 1000 ? val / 1000 + 'k' : val}`
          }
        }
      }
    }
  });
}

function renderCategoryDonutChart(categories) {
  const ctx = document.getElementById('chart-category-donut');
  if (!ctx) return;

  if (state.charts.category) {
    state.charts.category.destroy();
  }

  const isDark = state.theme === 'dark';
  const labels = Object.keys(categories || {});
  const values = Object.values(categories || {});

  const colors = [
    '#8b5cf6', '#3b82f6', '#10b981', '#f59e0b', '#f43f5e',
    '#ec4899', '#06b6d4', '#84cc16', '#a855f7'
  ];

  state.charts.category = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: labels.length ? labels : ['No Expenses Yet'],
      datasets: [{
        data: values.length ? values : [1],
        backgroundColor: labels.length ? colors.slice(0, labels.length) : ['rgba(150,150,150,0.15)'],
        borderColor: isDark ? '#0d1024' : '#ffffff',
        borderWidth: 2.5
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'right',
          labels: {
            color: isDark ? '#94a3b8' : '#475569',
            font: { family: 'Inter', size: 12 },
            padding: 12
          }
        },
        tooltip: {
          backgroundColor: isDark ? 'rgba(14, 18, 38, 0.95)' : 'rgba(255, 255, 255, 0.98)',
          titleColor: isDark ? '#fff' : '#0f172a',
          bodyColor: isDark ? '#cbd5e1' : '#334155',
          borderColor: 'rgba(139, 92, 246, 0.25)',
          borderWidth: 1,
          padding: 12,
          callbacks: {
            label: (ctx) => ` ${formatCurrency(ctx.raw)}`
          }
        }
      },
      cutout: '72%'
    }
  });
}

function renderRecentTransactions(transactions) {
  const tbody = document.getElementById('dash-recent-txns-body');
  if (!tbody) return;

  if (!transactions || transactions.length === 0) {
    tbody.innerHTML = `<tr><td colspan="5" class="empty-state-box">
      <div class="empty-state-icon">
        <svg viewBox="0 0 24 24" width="36" height="36" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" style="opacity: 0.5;">
          <rect x="2" y="5" width="20" height="14" rx="2"></rect>
          <line x1="2" y1="10" x2="22" y2="10"></line>
        </svg>
      </div>
      <div class="empty-state-title">No transactions recorded yet</div>
      <div class="empty-state-desc">Click "+ Add Transaction" to log your first income or expense.</div>
    </td></tr>`;
    return;
  }

  tbody.innerHTML = transactions.map(t => `
    <tr>
      <td><strong>${formatSafeText(t.date)}</strong></td>
      <td>${formatSafeText(t.description)}</td>
      <td><span class="badge-pill info">${formatSafeText(t.category)}</span></td>
      <td>${formatSafeText(t.paymentMethod, 'Cash')}</td>
      <td class="txn-amount ${(t.type || 'EXPENSE').toLowerCase()}">
        ${t.type === 'INCOME' ? '+' : '-'}${formatCurrency(t.amount)}
      </td>
    </tr>
  `).join('');
}

// ------------------------------------------------------------------------------
// 2. TRANSACTIONS LEDGER (FILTER, SEARCH, CRUD)
// ------------------------------------------------------------------------------
async function loadTransactions() {
  const res = await window.api.getTransactions();
  if (!res || !res.success) return;

  state.transactions = Array.isArray(res.data) ? res.data : [];
  populateCategoryFilter(state.transactions);
  applyTransactionFilters();
}

function populateCategoryFilter(transactions) {
  const select = document.getElementById('filter-txn-category');
  if (!select) return;

  const categories = Array.from(new Set(transactions.map(t => t.category).filter(Boolean)));
  select.innerHTML = `<option value="ALL">All Categories</option>` +
    categories.map(c => `<option value="${c}">${c}</option>`).join('');
}

function applyTransactionFilters() {
  const search = (document.getElementById('search-txn-input')?.value || '').toLowerCase();
  const typeFilter = document.getElementById('filter-txn-type')?.value || 'ALL';
  const catFilter = document.getElementById('filter-txn-category')?.value || 'ALL';

  state.filteredTransactions = state.transactions.filter(t => {
    const matchesSearch = !search ||
      (t.description && t.description.toLowerCase().includes(search)) ||
      (t.category && t.category.toLowerCase().includes(search)) ||
      (t.paymentMethod && t.paymentMethod.toLowerCase().includes(search));

    const matchesType = typeFilter === 'ALL' || t.type === typeFilter;
    const matchesCat = catFilter === 'ALL' || t.category === catFilter;

    return matchesSearch && matchesType && matchesCat;
  });

  renderTransactionsTable(state.filteredTransactions);
}

function renderTransactionsTable(list) {
  const tbody = document.getElementById('txns-table-body');
  if (!tbody) return;

  if (!list || list.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6">
      <div class="empty-state-box">
        <div class="empty-state-icon">
          <svg viewBox="0 0 24 24" width="36" height="36" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" style="opacity: 0.5;">
            <circle cx="11" cy="11" r="8"></circle>
            <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
          </svg>
        </div>
        <div class="empty-state-title">No matching transactions</div>
        <div class="empty-state-desc">Try modifying your search or filter criteria.</div>
      </div>
    </td></tr>`;
    return;
  }

  tbody.innerHTML = list.map(t => `
    <tr>
      <td>${formatSafeText(t.date)}</td>
      <td><strong>${formatSafeText(t.description)}</strong></td>
      <td><span class="badge-pill info">${formatSafeText(t.category)}</span></td>
      <td>${formatSafeText(t.paymentMethod, 'Cash')}</td>
      <td class="txn-amount ${(t.type || 'EXPENSE').toLowerCase()}">
        ${t.type === 'INCOME' ? '+' : '-'}${formatCurrency(t.amount)}
      </td>
      <td style="text-align: right;">
        <button class="btn-danger" onclick="handleDeleteTransaction(${t.transactionId})">Delete</button>
      </td>
    </tr>
  `).join('');
}

async function handleDeleteTransaction(id) {
  if (!confirm('Are you sure you want to delete this transaction?')) return;
  try {
    await window.api.deleteTransaction(id);
    showToast('Transaction removed', 'success');
    loadTransactions();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// ------------------------------------------------------------------------------
// 3. BUDGETS MANAGEMENT
// ------------------------------------------------------------------------------
async function loadBudgets() {
  const res = await window.api.getBudgets();
  if (!res.success) return;

  state.budgets = res.data;
  const grid = document.getElementById('budgets-grid');
  if (!grid) return;

  if (res.data.length === 0) {
    grid.innerHTML = `<div style="grid-column: 1/-1;">
      <div class="empty-state-box">
        <div class="empty-state-icon">
          <svg viewBox="0 0 24 24" width="36" height="36" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" style="opacity: 0.5;">
            <circle cx="12" cy="12" r="10"></circle>
            <circle cx="12" cy="12" r="6"></circle>
            <circle cx="12" cy="12" r="2"></circle>
          </svg>
        </div>
        <div class="empty-state-title">No category budgets established</div>
        <div class="empty-state-desc">Set spending limits to trigger real-time AI warnings before overspending.</div>
        <button class="btn-primary" style="margin-top: 12px;" onclick="openModal('modal-add-budget')">+ Set Budget</button>
      </div>
    </div>`;
    return;
  }

  grid.innerHTML = res.data.map(b => {
    const statusClass = b.isExceeded ? 'exceeded' : (b.isWarning ? 'warning' : 'normal');
    const badgeText = b.isExceeded ? 'EXCEEDED' : (b.isWarning ? 'WARNING' : 'ON TRACK');
    const badgeClass = b.isExceeded ? 'danger' : (b.isWarning ? 'warning' : 'success');

    return `
      <div class="glass-card" style="padding: 22px;">
        <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px;">
          <div>
            <h3 style="font-family: var(--font-display); font-size: 1.05rem; font-weight: 700; color: var(--text-pure);">${formatSafeText(b.category)}</h3>
            <span style="font-size: 0.72rem; color: var(--text-muted); text-transform: uppercase; font-weight: 600;">${formatSafeText(b.period, 'MONTHLY')} BUDGET</span>
          </div>
          <span class="badge-pill ${badgeClass}">${badgeText}</span>
        </div>
        
        <div style="margin: 14px 0;">
          <div style="display: flex; justify-content: space-between; font-size: 0.85rem; margin-bottom: 6px;">
            <span style="color: var(--text-secondary);">Spent: <strong>${formatCurrency(b.spentAmount)}</strong></span>
            <span style="color: var(--text-secondary);">Limit: <strong>${formatCurrency(b.budgetAmount)}</strong></span>
          </div>
          <div class="progress-bar-bg">
            <div class="progress-bar-fill ${statusClass}" style="width: ${Math.min(100, Number(b.percentageUsed) || 0)}%;"></div>
          </div>
          <div style="display: flex; justify-content: space-between; font-size: 0.75rem; margin-top: 6px; color: var(--text-muted);">
            <span>${b.percentageUsed || 0}% consumed</span>
            <span>Alert at ${b.warningThreshold || 80}%</span>
          </div>
        </div>

        <div style="display: flex; justify-content: flex-end; margin-top: 16px;">
          <button class="btn-danger" onclick="handleDeleteBudget(${b.budgetId})">Remove</button>
        </div>
      </div>
    `;
  }).join('');
}

async function handleDeleteBudget(id) {
  if (!confirm('Remove this budget limit?')) return;
  try {
    await window.api.deleteBudget(id);
    showToast('Budget removed', 'success');
    loadBudgets();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// ------------------------------------------------------------------------------
// 4. SAVINGS GOALS
// ------------------------------------------------------------------------------
async function loadGoals() {
  const res = await window.api.getGoals();
  if (!res.success) return;

  state.goals = res.data;
  const grid = document.getElementById('goals-grid');
  if (!grid) return;

  if (res.data.length === 0) {
    grid.innerHTML = `<div style="grid-column: 1/-1;">
      <div class="empty-state-box">
        <div class="empty-state-icon">
          <svg viewBox="0 0 24 24" width="36" height="36" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" style="opacity: 0.5;">
            <path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6"></path>
            <path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18"></path>
            <path d="M4 22h16"></path>
            <path d="M10 14.66V17c0 .55-.45 1-1 1H7"></path>
            <path d="M14 14.66V17c0 .55.45 1 1 1h2"></path>
            <path d="M18 2H6v7a6 6 0 0 0 12 0V2Z"></path>
          </svg>
        </div>
        <div class="empty-state-title">No financial goals defined</div>
        <div class="empty-state-desc">Set targeted savings milestones for vacations, emergencies, or large investments.</div>
        <button class="btn-primary" style="margin-top: 12px;" onclick="openModal('modal-add-goal')">+ New Goal</button>
      </div>
    </div>`;
    return;
  }

  grid.innerHTML = res.data.map(g => {
    const pct = g.targetAmount > 0 ? Math.min(100, Math.round((g.savedAmount / g.targetAmount) * 100)) : 0;
    return `
      <div class="glass-card" style="padding: 22px;">
        <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px;">
          <div>
            <h3 style="font-family: var(--font-display); font-size: 1.05rem; font-weight: 700; color: var(--text-pure);">${formatSafeText(g.goalName)}</h3>
            <span style="font-size: 0.72rem; color: var(--text-muted);">Deadline: ${formatSafeText(g.deadline)}</span>
          </div>
          <span class="badge-pill info">${formatSafeText(g.priority, 'MEDIUM')}</span>
        </div>

        <div style="margin: 14px 0;">
          <div style="display: flex; justify-content: space-between; font-size: 0.85rem; margin-bottom: 6px;">
            <span style="color: var(--text-secondary);">Saved: <strong>${formatCurrency(g.savedAmount)}</strong></span>
            <span style="color: var(--text-secondary);">Target: <strong>${formatCurrency(g.targetAmount)}</strong></span>
          </div>
          <div class="progress-bar-bg">
            <div class="progress-bar-fill normal" style="width: ${pct}%;"></div>
          </div>
          <div style="display: flex; justify-content: space-between; font-size: 0.75rem; margin-top: 6px; color: var(--text-muted);">
            <span>${pct}% completed</span>
            <span>Monthly Target: ${formatCurrency(g.monthlyContribution)}</span>
          </div>
        </div>

        <div style="display: flex; justify-content: flex-end; margin-top: 16px;">
          <button class="btn-primary" style="padding: 7px 16px; font-size: 0.8rem;" onclick="openContributeModal(${g.goalId}, '${(g.goalName || '').replace(/'/g, "\\'")}')">
            + Deposit Funds
          </button>
        </div>
      </div>
    `;
  }).join('');
}

function openContributeModal(id, name) {
  document.getElementById('contribute-goal-id').value = id;
  document.getElementById('contribute-goal-name').innerText = name;
  openModal('modal-goal-contribute');
}

// ------------------------------------------------------------------------------
// 5. INVESTMENTS & SIP SIMULATION
// ------------------------------------------------------------------------------
async function loadInvestments() {
  const res = await window.api.getInvestments();
  if (!res.success) return;

  state.investments = res.data;
  renderInvestmentsList(res.data);
  updateSIPCalculation();
}

function renderInvestmentsList(list) {
  const tbody = document.getElementById('invest-table-body');
  if (!tbody) return;

  if (!list || list.length === 0) {
    tbody.innerHTML = `<tr><td colspan="4">
      <div class="empty-state-box">
        <div class="empty-state-icon">
          <svg viewBox="0 0 24 24" width="36" height="36" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" style="opacity: 0.5;">
            <polyline points="23 6 13.5 15.5 8.5 10.5 1 18"></polyline>
            <polyline points="17 6 23 6 23 12"></polyline>
          </svg>
        </div>
        <div class="empty-state-title">No investment holdings logged</div>
        <div class="empty-state-desc">Record your Mutual Funds, Stocks, FDs, or Gold to track portfolio growth.</div>
      </div>
    </td></tr>`;
    return;
  }

  tbody.innerHTML = list.map(inv => `
    <tr>
      <td><strong>${formatSafeText(inv.name || inv.type)}</strong></td>
      <td class="tabular-num">${formatCurrency(inv.amount)}</td>
      <td><span class="badge-pill success">${inv.returnRate || 0}% p.a.</span></td>
      <td>${formatSafeText(inv.startDate)}</td>
    </tr>
  `).join('');
}

function updateSIPCalculation() {
  const monthly = Number(document.getElementById('sip-monthly')?.value || 5000);
  const rate = Number(document.getElementById('sip-rate')?.value || 12);
  const years = Number(document.getElementById('sip-years')?.value || 10);

  const monthlyLabel = document.getElementById('sip-monthly-label');
  const rateLabel = document.getElementById('sip-rate-label');
  const yearsLabel = document.getElementById('sip-years-label');

  if (monthlyLabel) monthlyLabel.innerText = formatCurrency(monthly);
  if (rateLabel) rateLabel.innerText = `${rate}%`;
  if (yearsLabel) yearsLabel.innerText = `${years} Year${years > 1 ? 's' : ''}`;

  // Compound Monthly SIP Formula: M = P * ({[1 + i]^n - 1} / i) * (1 + i)
  const i = (rate / 100) / 12;
  const n = years * 12;
  const totalInvested = monthly * n;
  let maturityAmount = 0;

  if (i > 0) {
    maturityAmount = monthly * ((Math.pow(1 + i, n) - 1) / i) * (1 + i);
  } else {
    maturityAmount = totalInvested;
  }

  const totalReturns = Math.max(0, maturityAmount - totalInvested);

  const invVal = document.getElementById('sip-invested-val');
  const retVal = document.getElementById('sip-returns-val');
  const matVal = document.getElementById('sip-maturity-val');

  if (invVal) invVal.innerText = formatCurrency(totalInvested);
  if (retVal) retVal.innerText = formatCurrency(totalReturns);
  if (matVal) matVal.innerText = formatCurrency(maturityAmount);

  renderSIPChart(totalInvested, totalReturns);
}

function renderSIPChart(invested, returns) {
  const ctx = document.getElementById('chart-sip');
  if (!ctx) return;

  if (state.charts.sip) {
    state.charts.sip.destroy();
  }

  const isDark = state.theme === 'dark';

  state.charts.sip = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: ['Invested Principal', 'Estimated Compound Growth'],
      datasets: [{
        data: [Math.round(invested), Math.round(returns)],
        backgroundColor: ['#6366f1', '#10b981'],
        borderColor: isDark ? '#0d1024' : '#ffffff',
        borderWidth: 3
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            color: isDark ? '#94a3b8' : '#475569',
            font: { family: 'Inter', size: 12 },
            padding: 12
          }
        },
        tooltip: {
          backgroundColor: isDark ? 'rgba(14, 18, 38, 0.95)' : 'rgba(255, 255, 255, 0.98)',
          titleColor: isDark ? '#fff' : '#0f172a',
          bodyColor: isDark ? '#cbd5e1' : '#334155',
          borderColor: 'rgba(139, 92, 246, 0.25)',
          borderWidth: 1,
          padding: 12,
          callbacks: {
            label: (ctx) => ` ${formatCurrency(ctx.raw)}`
          }
        }
      },
      cutout: '70%'
    }
  });
}

// ------------------------------------------------------------------------------
// 6. NET WORTH & LIABILITIES
// ------------------------------------------------------------------------------
async function loadNetWorth() {
  const [nwRes, assetsRes, liabilitiesRes] = await Promise.all([
    window.api.getNetWorth(),
    window.api.getAssets(),
    window.api.getLiabilities()
  ]);

  if (nwRes.success) {
    const d = nwRes.data;
    state.networth = d;
    document.getElementById('nw-total-assets').innerText = formatCurrency(d.totalAssets);
    document.getElementById('nw-total-liabilities').innerText = formatCurrency(d.totalLiabilities);
    document.getElementById('nw-net-worth').innerText = formatCurrency(d.netWorth);
    document.getElementById('nw-debt-ratio').innerText = `${d.debtToAssetRatio || 0}%`;
  }

  // Assets Table
  const assetsBody = document.getElementById('assets-table-body');
  if (assetsBody && assetsRes.success) {
    assetsBody.innerHTML = assetsRes.data.length ? assetsRes.data.map(a => `
      <tr>
        <td><strong>${formatSafeText(a.name)}</strong></td>
        <td><span class="badge-pill info">${formatSafeText(a.type)}</span></td>
        <td class="tabular-num">${formatCurrency(a.value)}</td>
        <td>${formatSafeText(a.notes)}</td>
      </tr>
    `).join('') : `<tr><td colspan="4" class="empty-state-box">No assets recorded.</td></tr>`;
  }

  // Liabilities Table
  const liabBody = document.getElementById('liabilities-table-body');
  if (liabBody && liabilitiesRes.success) {
    liabBody.innerHTML = liabilitiesRes.data.length ? liabilitiesRes.data.map(l => `
      <tr>
        <td><strong>${formatSafeText(l.name)}</strong></td>
        <td class="tabular-num">${formatCurrency(l.principal)}</td>
        <td class="tabular-num">${formatCurrency(l.remainingBalance)}</td>
        <td class="tabular-num">${formatCurrency(l.emi)}/mo</td>
        <td>${l.interestRate || 0}%</td>
      </tr>
    `).join('') : `<tr><td colspan="5" class="empty-state-box">No active loans or liabilities.</td></tr>`;
  }
}

// ------------------------------------------------------------------------------
// 7. SUBSCRIPTIONS VIEW
// ------------------------------------------------------------------------------
async function loadSubscriptions() {
  const res = await window.api.getSubscriptions();
  if (!res.success) return;

  state.subscriptions = res.data;
  const tbody = document.getElementById('subs-table-body');
  if (!tbody) return;

  let monthlyTotal = 0;
  res.data.forEach(s => {
    const amt = Number(s.amount) || 0;
    if (s.billingCycle === 'YEARLY') monthlyTotal += amt / 12;
    else if (s.billingCycle === 'QUARTERLY') monthlyTotal += amt / 3;
    else monthlyTotal += amt;
  });

  const totalEl = document.getElementById('subs-monthly-total');
  if (totalEl) totalEl.innerText = formatCurrency(monthlyTotal);

  if (res.data.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6">
      <div class="empty-state-box">
        <div class="empty-state-icon">
          <svg viewBox="0 0 24 24" width="36" height="36" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" style="opacity: 0.5;">
            <polyline points="23 4 23 10 17 10"></polyline>
            <polyline points="1 20 1 14 7 14"></polyline>
            <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
          </svg>
        </div>
        <div class="empty-state-title">No recurring subscriptions active</div>
        <div class="empty-state-desc">Track Netflix, Spotify, gym, and SaaS memberships to monitor monthly recurring burn.</div>
      </div>
    </td></tr>`;
    return;
  }

  tbody.innerHTML = res.data.map(s => `
    <tr>
      <td><strong>${formatSafeText(s.serviceName)}</strong></td>
      <td class="tabular-num">${formatCurrency(s.amount)}</td>
      <td><span class="badge-pill info">${formatSafeText(s.billingCycle, 'MONTHLY')}</span></td>
      <td>${formatSafeText(s.nextBillingDate)}</td>
      <td><span class="badge-pill success">${formatSafeText(s.status, 'ACTIVE')}</span></td>
      <td style="text-align: right;">
        <button class="btn-danger" onclick="handleDeleteSubscription(${s.subscriptionId})">Cancel</button>
      </td>
    </tr>
  `).join('');
}

async function handleDeleteSubscription(id) {
  if (!confirm('Cancel tracking for this subscription?')) return;
  try {
    await window.api.deleteSubscription(id);
    showToast('Subscription removed', 'success');
    loadSubscriptions();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// ------------------------------------------------------------------------------
// 8. AI FINANCIAL ADVISOR
// ------------------------------------------------------------------------------
async function loadAiInsights() {
  const res = await window.api.getAiInsights();
  if (!res.success) return;

  const container = document.getElementById('ai-insights-list');
  const suggestionsContainer = document.getElementById('ai-suggestions-list');

  if (container && res.data.insights) {
    container.innerHTML = res.data.insights.map(item => {
      const priorityType = (item.priority || 'INFO').toLowerCase();
      return `
        <div class="ai-card ${priorityType}">
          <div class="ai-card-icon">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="m12 3-1.9 5.8a2 2 0 0 1-1.3 1.3L3 12l5.8 1.9a2 2 0 0 1 1.3 1.3L12 21l1.9-5.8a2 2 0 0 1 1.3-1.3L21 12l-5.8-1.9a2 2 0 0 1-1.3-1.3Z"></path>
            </svg>
          </div>
          <div class="ai-card-content">
            <h4>${formatSafeText(item.title)}</h4>
            <p>${formatSafeText(item.message)}</p>
          </div>
        </div>
      `;
    }).join('');
  }

  if (suggestionsContainer && res.data.suggestions) {
    suggestionsContainer.innerHTML = res.data.suggestions.map(s => `
      <li style="padding: 12px 0; border-bottom: 1px solid var(--border-glass); color: var(--text-primary); font-size: 0.88rem; display: flex; align-items: flex-start; gap: 12px;">
        <span style="color: var(--accent-cyan); display: flex; align-items: center; margin-top: 2px;">
          <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
            <circle cx="12" cy="12" r="4"></circle>
          </svg>
        </span>
        <span>${formatSafeText(s)}</span>
      </li>
    `).join('');
  }
}

// ------------------------------------------------------------------------------
// LIVE AIVEN DB HEARTBEAT
// ------------------------------------------------------------------------------
async function pollDbHealth() {
  try {
    const res = await window.api.checkHealth();
    const pill = document.getElementById('header-db-pill');
    if (pill) {
      const isConnected = !!(
        res &&
        (res.databaseConnected ||
         res.dbConnected ||
         (res.data && (res.data.databaseConnected || res.data.dbConnected)) ||
         (res.data && res.data.status === 'UP') ||
         res.success)
      );
      if (isConnected) {
        pill.innerHTML = `<span class="db-dot"></span><span>Aiven MySQL Cloud (SSL Active)</span>`;
      } else {
        pill.innerHTML = `<span class="db-dot" style="background: var(--danger);"></span><span>DB Disconnected</span>`;
      }
    }
  } catch (e) {
    const pill = document.getElementById('header-db-pill');
    if (pill) {
      pill.innerHTML = `<span class="db-dot" style="background: var(--danger);"></span><span>DB Disconnected</span>`;
    }
  }
}

function refreshAllCharts() {
  if (state.dashboardData) {
    renderCategoryDonutChart(state.dashboardData.categoryExpenses);
    renderIncomeExpenseBarChart(state.dashboardData.totalIncome, state.dashboardData.totalExpenses);
  }
  updateSIPCalculation();
}

// ------------------------------------------------------------------------------
// AUTHENTICATION FLOW & FULL-SCREEN VIEW TOGGLE
// ------------------------------------------------------------------------------
function showAuthContainer(show) {
  const container = document.getElementById('auth-full-container');
  if (container) {
    if (show) {
      container.classList.remove('hidden');
    } else {
      container.classList.add('hidden');
    }
  }
}

function togglePasswordVisibility(fieldId, iconId) {
  const field = document.getElementById(fieldId);
  const icon = document.getElementById(iconId);
  const textEl = document.getElementById('auth-pwd-text');
  if (field && icon) {
    if (field.type === 'password') {
      field.type = 'text';
      icon.innerHTML = `<svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>`;
      if (textEl) textEl.innerText = 'Hide';
    } else {
      field.type = 'password';
      icon.innerHTML = `<svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>`;
      if (textEl) textEl.innerText = 'Show';
    }
  }
}

// Demo One-Click Fill
function fillDemoCredentials() {
  const emailInp = document.getElementById('auth-email');
  const passInp = document.getElementById('auth-password');
  if (emailInp && passInp) {
    emailInp.value = 'testuser@finvisiq.com';
    passInp.value = 'Password@123';
    showToast('Demo credentials autofilled!', 'info');
  }
}

// ------------------------------------------------------------------------------
// INITIALIZATION & EVENT BINDINGS
// ------------------------------------------------------------------------------
document.addEventListener('DOMContentLoaded', () => {
  // 1. Initialize Theme (Dark / Light)
  initTheme();

  // 2. Navigation Click Listeners
  document.querySelectorAll('.nav-item, .mobile-nav-item').forEach(item => {
    item.addEventListener('click', (e) => {
      e.preventDefault();
      const view = item.dataset.view;
      if (view) switchView(view);
      document.querySelector('.sidebar')?.classList.remove('mobile-open');
    });
  });

  // Mobile Menu Toggle Button
  const mobileToggle = document.getElementById('btn-mobile-menu');
  if (mobileToggle) {
    mobileToggle.addEventListener('click', () => {
      document.querySelector('.sidebar')?.classList.toggle('mobile-open');
    });
  }

  // Filter & Search Listeners for Transactions Ledger
  document.getElementById('search-txn-input')?.addEventListener('input', applyTransactionFilters);
  document.getElementById('filter-txn-type')?.addEventListener('change', applyTransactionFilters);
  document.getElementById('filter-txn-category')?.addEventListener('change', applyTransactionFilters);

  // Auth State Listener
  window.addEventListener('finvisiq:auth-changed', (e) => {
    if (e.detail.loggedIn) {
      const user = window.api.getUser();
      if (user) {
        document.getElementById('user-display-name').innerText = formatSafeText(user.name, 'Akilesh');
        document.getElementById('user-display-email').innerText = formatSafeText(user.email, '');
        document.getElementById('user-avatar').innerText = (user.name || 'U').charAt(0).toUpperCase();
      }
      showAuthContainer(false);
      state.hasAnimatedNumbers = false;
      switchView(state.activeView);
    } else {
      showAuthContainer(true);
    }
  });

  window.addEventListener('finvisiq:unauthorized', () => {
    showAuthContainer(true);
  });

  // Auth Tabs Toggle (Sign In vs Create Account)
  let isRegisterMode = false;
  const tabSignIn = document.getElementById('tab-sign-in');
  const tabSignUp = document.getElementById('tab-sign-up');
  const nameGroup = document.getElementById('auth-name-group');
  const authSubmit = document.getElementById('auth-submit-btn');
  const authAlert = document.getElementById('auth-error-alert');
  const authMsg = document.getElementById('auth-error-msg');

  function setAuthMode(register) {
    isRegisterMode = register;
    if (tabSignIn && tabSignUp) {
      tabSignIn.classList.toggle('active', !register);
      tabSignUp.classList.toggle('active', register);
    }
    if (nameGroup) nameGroup.style.display = register ? 'flex' : 'none';
    if (authSubmit) authSubmit.innerText = register ? 'Create Free Account' : 'Sign In to FinvisIQ';
    if (authAlert) authAlert.style.display = 'none';
  }

  tabSignIn?.addEventListener('click', () => setAuthMode(false));
  tabSignUp?.addEventListener('click', () => setAuthMode(true));

  // Form Submissions

  // A. Authentication Form
  document.getElementById('form-auth')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (authAlert) authAlert.style.display = 'none';

    const email = document.getElementById('auth-email').value.trim();
    const password = document.getElementById('auth-password').value;
    const name = document.getElementById('auth-name')?.value.trim();

    if (authSubmit) {
      authSubmit.disabled = true;
      authSubmit.innerText = 'Connecting to Cloud...';
    }

    try {
      if (isRegisterMode) {
        await window.api.register(name, email, password);
        showToast('Welcome to FinvisIQ!', 'success');
      } else {
        await window.api.login(email, password);
        showToast('Signed in successfully', 'success');
      }
    } catch (err) {
      if (authMsg) {
        authMsg.innerText = err.message || 'Authentication failed. Check credentials.';
      }
      if (authAlert) {
        authAlert.style.display = 'flex';
      }
      showToast(err.message, 'error');
    } finally {
      if (authSubmit) {
        authSubmit.disabled = false;
        authSubmit.innerText = isRegisterMode ? 'Create Free Account' : 'Sign In to FinvisIQ';
      }
    }
  });

  // B. Transaction Form
  document.getElementById('form-add-txn')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const data = {
      type: document.getElementById('txn-type').value,
      amount: Number(document.getElementById('txn-amount').value),
      category: document.getElementById('txn-category').value,
      date: document.getElementById('txn-date').value,
      description: document.getElementById('txn-desc').value,
      paymentMethod: document.getElementById('txn-payment').value
    };

    try {
      await window.api.createTransaction(data);
      showToast('Transaction saved to cloud database!', 'success');
      closeModal('modal-add-txn');
      e.target.reset();
      loadViewData(state.activeView);
    } catch (err) {
      showToast(err.message, 'error');
    }
  });

  // C. Budget Form
  document.getElementById('form-add-budget')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const data = {
      category: document.getElementById('budget-category').value,
      budgetAmount: Number(document.getElementById('budget-amount').value),
      period: document.getElementById('budget-period').value,
      warningThreshold: Number(document.getElementById('budget-threshold').value)
    };

    try {
      await window.api.createBudget(data);
      showToast('Budget allocated!', 'success');
      closeModal('modal-add-budget');
      e.target.reset();
      loadBudgets();
    } catch (err) {
      showToast(err.message, 'error');
    }
  });

  // D. Goal Form
  document.getElementById('form-add-goal')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const data = {
      goalName: document.getElementById('goal-name').value,
      targetAmount: Number(document.getElementById('goal-target').value),
      savedAmount: Number(document.getElementById('goal-saved').value || 0),
      deadline: document.getElementById('goal-deadline').value,
      priority: document.getElementById('goal-priority').value,
      category: document.getElementById('goal-category').value,
      monthlyContribution: Number(document.getElementById('goal-monthly').value || 0),
      expectedReturn: Number(document.getElementById('goal-return').value || 0)
    };

    try {
      await window.api.createGoal(data);
      showToast('Savings goal created!', 'success');
      closeModal('modal-add-goal');
      e.target.reset();
      loadGoals();
    } catch (err) {
      showToast(err.message, 'error');
    }
  });

  // E. Goal Contribution Form
  document.getElementById('form-contribute-goal')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('contribute-goal-id').value;
    const amount = document.getElementById('contribute-amount').value;

    try {
      await window.api.contributeToGoal(id, amount);
      showToast('Funds contributed to goal!', 'success');
      closeModal('modal-goal-contribute');
      loadGoals();
    } catch (err) {
      showToast(err.message, 'error');
    }
  });

  // F. Investment Form
  document.getElementById('form-add-invest')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const data = {
      name: document.getElementById('invest-name').value,
      type: document.getElementById('invest-type').value,
      amount: Number(document.getElementById('invest-amount').value),
      returnRate: Number(document.getElementById('invest-rate').value),
      startDate: document.getElementById('invest-date').value
    };

    try {
      await window.api.createInvestment(data);
      showToast('Investment added to portfolio!', 'success');
      closeModal('modal-add-invest');
      e.target.reset();
      loadInvestments();
    } catch (err) {
      showToast(err.message, 'error');
    }
  });

  // G. Subscription Form
  document.getElementById('form-add-sub')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const data = {
      serviceName: document.getElementById('sub-name').value,
      amount: Number(document.getElementById('sub-amount').value),
      billingCycle: document.getElementById('sub-cycle').value,
      nextBillingDate: document.getElementById('sub-date').value,
      category: document.getElementById('sub-cat').value
    };

    try {
      await window.api.createSubscription(data);
      showToast('Subscription tracked!', 'success');
      closeModal('modal-add-sub');
      e.target.reset();
      loadSubscriptions();
    } catch (err) {
      showToast(err.message, 'error');
    }
  });

  // H. Asset Form
  document.getElementById('form-add-asset')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const data = {
      name: document.getElementById('asset-name').value,
      type: document.getElementById('asset-type').value,
      value: Number(document.getElementById('asset-val').value),
      notes: document.getElementById('asset-notes').value
    };

    try {
      await window.api.createAsset(data);
      showToast('Asset added to net worth portfolio!', 'success');
      closeModal('modal-add-asset');
      e.target.reset();
      loadNetWorth();
    } catch (err) {
      showToast(err.message, 'error');
    }
  });

  // I. Liability Form
  document.getElementById('form-add-liability')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const data = {
      name: document.getElementById('liab-name').value,
      principal: Number(document.getElementById('liab-principal').value),
      remainingBalance: Number(document.getElementById('liab-remaining').value),
      emi: Number(document.getElementById('liab-emi').value),
      interestRate: Number(document.getElementById('liab-rate').value)
    };

    try {
      await window.api.createLiability(data);
      showToast('Loan added to liabilities register!', 'success');
      closeModal('modal-add-liability');
      e.target.reset();
      loadNetWorth();
    } catch (err) {
      showToast(err.message, 'error');
    }
  });

  // SIP Calculator Range Sliders
  ['sip-monthly', 'sip-rate', 'sip-years'].forEach(id => {
    document.getElementById(id)?.addEventListener('input', updateSIPCalculation);
  });

  // Set default dates
  const todayStr = new Date().toISOString().split('T')[0];
  document.querySelectorAll('input[type="date"]').forEach(inp => {
    if (!inp.value) inp.value = todayStr;
  });

  // Keyboard accessibility: Escape closes any open modal
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      document.querySelectorAll('.modal-backdrop.open').forEach(modal => {
        modal.classList.remove('open');
      });
    }
  });

  // Initial App State Resolution
  if (window.api.isLoggedIn()) {
    showAuthContainer(false);
    const user = window.api.getUser();
    if (user) {
      document.getElementById('user-display-name').innerText = formatSafeText(user.name, 'Akilesh');
      document.getElementById('user-display-email').innerText = formatSafeText(user.email, '');
      document.getElementById('user-avatar').innerText = (user.name || 'U').charAt(0).toUpperCase();
    }
    switchView('dashboard');
  } else {
    showAuthContainer(true);
  }

  // Live Database Heartbeat
  pollDbHealth();
  setInterval(pollDbHealth, 30000);
});
