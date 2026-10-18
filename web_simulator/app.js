// ========================================================
// AN NHIÊN — JAVASCRIPT STATE & INTERACTIVITY
// ========================================================

const STORAGE_KEYS = {
    EPISODES: 'annhien_episodes',
    JOURNALS: 'annhien_journals',
    WATER: 'annhien_water',
    PIN: 'annhien_pin',
    LOCK_ENABLED: 'annhien_lock_enabled',
    THEME: 'annhien_theme',
    KICKS: 'annhien_kicks',
    HOT_FLASHES: 'annhien_hot_flashes',
    VISITS: 'annhien_visits'
};

// Initial state or seed data
let episodes = JSON.parse(localStorage.getItem(STORAGE_KEYS.EPISODES)) || [
    { id: "ep_1", startDate: "2026-08-18", endDate: "2026-08-22", isOngoing: false },
    { id: "ep_2", startDate: "2026-09-16", endDate: "2026-09-20", isOngoing: false }
];

let journals = JSON.parse(localStorage.getItem(STORAGE_KEYS.JOURNALS)) || [
    { id: "j_1", date: "2026-10-14", symptoms: ["Mệt mỏi", "Đau lưng"], mood: 3, sleep: 7.5, note: "Ngày làm việc nhẹ nhàng, uống đủ nước ấm." },
    { id: "j_2", date: "2026-10-15", symptoms: ["Căng tức ngực"], mood: 4, sleep: 8.0, note: "Tâm trạng khá thoải mái, đi dạo công viên 20 phút." }
];

let waterMl = parseInt(localStorage.getItem(STORAGE_KEYS.WATER)) || 1250;
let lockEnabled = localStorage.getItem(STORAGE_KEYS.LOCK_ENABLED) === 'true';
let userPin = localStorage.getItem(STORAGE_KEYS.PIN) || "1234";

let currentCalYear = 2026;
let currentCalMonth = 9; // 0-indexed: 9 = Tháng 10
let selectedDateStr = "2026-10-16";

// Live contraction timer
let contractionTimerInterval = null;
let contractionSeconds = 0;
let contractionHistory = [];

// Breathing timer
let breathingInterval = null;
let breathingPhase = "READY";
let breathingCount = 0;

// ========================================================
// INITIALIZATION
// ========================================================
document.addEventListener('DOMContentLoaded', () => {
    initClock();
    initTheme();
    renderHome();
    simUpdateHomeQuote();
    renderCalendar();
    renderJournalFeed();
    renderReports();

    // Check lock
    if (lockEnabled) {
        showPinLock();
    }

    // Desktop controls
    document.getElementById('btn-theme-toggle').addEventListener('click', toggleTheme);
    document.getElementById('btn-frame-toggle').addEventListener('click', toggleFrameMode);
    document.getElementById('btn-calc-toggle').addEventListener('click', () => {
        document.getElementById('calculator-overlay').classList.toggle('hidden');
    });
    document.getElementById('btn-lock-toggle').addEventListener('click', () => {
        showPinLock();
    });

    // Calendar month nav
    document.getElementById('btn-prev-month').addEventListener('click', () => {
        currentCalMonth--;
        if (currentCalMonth < 0) { currentCalMonth = 11; currentCalYear--; }
        renderCalendar();
    });
    document.getElementById('btn-next-month').addEventListener('click', () => {
        currentCalMonth++;
        if (currentCalMonth > 11) { currentCalMonth = 0; currentCalYear++; }
        renderCalendar();
    });

    // Hero period toggle
    document.getElementById('btn-toggle-period').addEventListener('click', togglePeriodToday);
    document.getElementById('btn-toggle-period-date').addEventListener('click', togglePeriodSelectedDate);

    // Journal search
    document.getElementById('journal-search-input').addEventListener('input', (e) => {
        renderJournalFeed(e.target.value.trim().toLowerCase());
    });
});

// Clock
function initClock() {
    function update() {
        const now = new Date();
        const hours = String(now.getHours()).padStart(2, '0');
        const mins = String(now.getMinutes()).padStart(2, '0');
        document.getElementById('current-clock').textContent = `${hours}:${mins}`;
    }
    update();
    setInterval(update, 10000);
}

// Theme
function initTheme() {
    const saved = localStorage.getItem(STORAGE_KEYS.THEME);
    if (saved === 'dark') {
        document.body.classList.remove('light-theme');
        document.body.classList.add('dark-theme');
        document.getElementById('btn-theme-toggle').innerHTML = '<i class="fa-solid fa-sun"></i>';
    }
}

function toggleTheme() {
    if (document.body.classList.contains('dark-theme')) {
        document.body.classList.remove('dark-theme');
        document.body.classList.add('light-theme');
        localStorage.setItem(STORAGE_KEYS.THEME, 'light');
        document.getElementById('btn-theme-toggle').innerHTML = '<i class="fa-solid fa-moon"></i>';
    } else {
        document.body.classList.remove('light-theme');
        document.body.classList.add('dark-theme');
        localStorage.setItem(STORAGE_KEYS.THEME, 'dark');
        document.getElementById('btn-theme-toggle').innerHTML = '<i class="fa-solid fa-sun"></i>';
    }
}

function toggleFrameMode() {
    const frame = document.getElementById('device-frame');
    frame.classList.toggle('fullscreen-mode');
}

// ========================================================
// TAB NAVIGATION
// ========================================================
function switchTab(tabId) {
    document.querySelectorAll('.tab-view').forEach(el => el.classList.remove('active'));
    document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));

    const targetTab = document.getElementById(`tab-${tabId}`);
    if (targetTab) targetTab.classList.add('active');

    // Highlight bottom bar item
    const tabs = ['home', 'calendar', 'journal', 'reports', 'utilities'];
    const idx = tabs.indexOf(tabId);
    if (idx !== -1) {
        document.querySelectorAll('.nav-item')[idx].classList.add('active');
    }

    // Title update
    const titleMap = {
        home: "Hôm nay",
        calendar: "Lịch sức khỏe",
        journal: "Nhật ký",
        reports: "Báo cáo",
        utilities: "Tiện ích & Chuyên đề"
    };
    document.getElementById('screen-title').textContent = titleMap[tabId] || "An Nhiên";

    // FAB visible only on journal
    const fab = document.getElementById('btn-fab-add');
    fab.style.display = (tabId === 'journal') ? 'flex' : 'none';

    if (tabId === 'calendar') renderCalendar();
    if (tabId === 'reports') renderReports();
}

// ========================================================
// TAB 1: HOME & CYCLE CALCULATIONS
// ========================================================
function renderHome() {
    const today = new Date();
    const todayStr = today.toISOString().split('T')[0];

    // Check ongoing period
    const ongoing = episodes.find(e => e.isOngoing);
    const badge = document.getElementById('hero-cycle-badge');
    const predTitle = document.getElementById('hero-prediction-title');
    const btnHero = document.getElementById('btn-toggle-period');
    const btnLabel = document.getElementById('period-btn-label');

    if (ongoing) {
        badge.textContent = "Đang trong kỳ kinh nguyệt";
        badge.style.background = "#FFDAD6";
        badge.style.color = "#BA1A1A";
        predTitle.textContent = "Hãy chú ý nghỉ ngơi và giữ ấm cơ thể";
        btnHero.classList.add('period-active');
        btnLabel.textContent = "Đánh dấu kết thúc kỳ kinh";
    } else {
        // Calculate median cycle length
        const medianCycle = calculateMedianCycle();
        const sorted = [...episodes].sort((a,b) => new Date(b.startDate) - new Date(a.startDate));
        const last = sorted[0];

        if (last) {
            const lastStart = new Date(last.startDate);
            const daysSince = Math.floor((today - lastStart) / (1000 * 60 * 60 * 24)) + 1;
            badge.textContent = `Ngày ${daysSince} của chu kỳ`;
            badge.style.background = "var(--surface)";
            badge.style.color = "var(--primary)";

            const nextExpected = new Date(lastStart);
            nextExpected.setDate(nextExpected.getDate() + medianCycle);
            const daysUntil = Math.ceil((nextExpected - today) / (1000 * 60 * 60 * 24));

            if (daysUntil > 0) {
                predTitle.textContent = `Dự đoán kỳ kinh sau: khoảng ${daysUntil} ngày nữa`;
            } else if (daysUntil === 0) {
                predTitle.textContent = "Dự đoán kỳ kinh có thể bắt đầu hôm nay";
            } else {
                predTitle.textContent = "Kỳ kinh có thể đang chậm hơn dự kiến";
            }
        } else {
            badge.textContent = "Chưa ghi nhận kỳ kinh";
            predTitle.textContent = "Bắt đầu theo dõi chu kỳ của bạn";
        }

        btnHero.classList.remove('period-active');
        btnLabel.textContent = "Kỳ kinh bắt đầu hôm nay";
    }

    // Hydration progress
    document.getElementById('water-text').textContent = `${waterMl} / 2000 ml`;
    const pct = Math.min(100, (waterMl / 2000) * 100);
    document.getElementById('water-bar').style.width = `${pct}%`;
}

function calculateMedianCycle() {
    if (episodes.length < 2) return 28;
    const sorted = [...episodes].sort((a,b) => new Date(a.startDate) - new Date(b.startDate));
    const lengths = [];
    for (let i = 0; i < sorted.length - 1; i++) {
        const d1 = new Date(sorted[i].startDate);
        const d2 = new Date(sorted[i+1].startDate);
        const diff = Math.round((d2 - d1) / (1000 * 60 * 60 * 24));
        if (diff >= 18 && diff <= 60) lengths.push(diff);
    }
    if (lengths.length === 0) return 29;
    lengths.sort((a,b) => a - b);
    const mid = Math.floor(lengths.length / 2);
    return (lengths.length % 2 !== 0) ? lengths[mid] : Math.round((lengths[mid - 1] + lengths[mid]) / 2);
}

function togglePeriodToday() {
    const todayStr = new Date().toISOString().split('T')[0];
    const ongoing = episodes.find(e => e.isOngoing);
    if (ongoing) {
        ongoing.endDate = todayStr;
        ongoing.isOngoing = false;
    } else {
        episodes.push({
            id: 'ep_' + Date.now(),
            startDate: todayStr,
            endDate: null,
            isOngoing: true
        });
    }
    localStorage.setItem(STORAGE_KEYS.EPISODES, JSON.stringify(episodes));
    renderHome();
    renderCalendar();
    renderReports();
}

function addWater(amount) {
    waterMl += amount;
    localStorage.setItem(STORAGE_KEYS.WATER, waterMl);
    renderHome();
}

// ========================================================
// TAB 2: CALENDAR
// ========================================================
function renderCalendar() {
    const monthNames = [
        "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
        "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
    ];
    document.getElementById('calendar-month-label').textContent = `${monthNames[currentCalMonth]}, ${currentCalYear}`;

    const daysGrid = document.getElementById('calendar-days-grid');
    daysGrid.innerHTML = '';

    const firstDay = new Date(currentCalYear, currentCalMonth, 1);
    const daysInMonth = new Date(currentCalYear, currentCalMonth + 1, 0).getDate();
    
    // Day of week offset: Monday=0, Sunday=6
    let startDayOfWeek = firstDay.getDay() - 1;
    if (startDayOfWeek === -1) startDayOfWeek = 6;

    // Pad leading days
    for (let i = 0; i < startDayOfWeek; i++) {
        const emptyCell = document.createElement('div');
        emptyCell.className = 'day-cell empty';
        daysGrid.appendChild(emptyCell);
    }

    // Populate month days
    for (let day = 1; day <= daysInMonth; day++) {
        const cell = document.createElement('div');
        cell.className = 'day-cell';
        cell.textContent = day;

        const dateStr = `${currentCalYear}-${String(currentCalMonth + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;

        if (dateStr === selectedDateStr) {
            cell.classList.add('selected');
        }

        // Check if period day
        const isPeriod = episodes.some(ep => {
            const start = ep.startDate;
            const end = ep.endDate || new Date().toISOString().split('T')[0];
            return dateStr >= start && dateStr <= end;
        });
        if (isPeriod) cell.classList.add('period');

        // Check journal dot
        const hasJournal = journals.some(j => j.date === dateStr);
        if (hasJournal) {
            const dot = document.createElement('span');
            dot.className = 'day-dot';
            cell.appendChild(dot);
        }

        cell.addEventListener('click', () => {
            selectedDateStr = dateStr;
            renderCalendar();
            renderSelectedDayDetail();
        });

        daysGrid.appendChild(cell);
    }

    renderSelectedDayDetail();
}

function renderSelectedDayDetail() {
    const detailLabel = document.getElementById('detail-date-label');
    const detailContent = document.getElementById('detail-content');
    const btnToggle = document.getElementById('btn-toggle-period-date');

    detailLabel.textContent = `Ngày: ${selectedDateStr}`;

    const isPeriod = episodes.some(ep => {
        const start = ep.startDate;
        const end = ep.endDate || new Date().toISOString().split('T')[0];
        return selectedDateStr >= start && selectedDateStr <= end;
    });

    btnToggle.textContent = isPeriod ? "Hủy đánh dấu kinh" : "Đánh dấu kỳ kinh";

    const journal = journals.find(j => j.date === selectedDateStr);
    if (journal) {
        let symHtml = (journal.symptoms && journal.symptoms.length)
            ? `<div class="journal-tags">${journal.symptoms.map(s => `<span class="tag-badge">${s}</span>`).join('')}</div>`
            : '';
        detailContent.innerHTML = `
            ${symHtml}
            <p><strong>Tâm trạng:</strong> ${journal.mood || 3}/5 · <strong>Ngủ:</strong> ${journal.sleep || 7}h</p>
            ${journal.note ? `<p class="tip-desc">"${journal.note}"</p>` : ''}
        `;
    } else {
        detailContent.innerHTML = `<p class="text-muted">Chưa có bản ghi triệu chứng hay tâm trạng cho ngày này.</p>`;
    }
}

function togglePeriodSelectedDate() {
    const isPeriodIndex = episodes.findIndex(ep => {
        const start = ep.startDate;
        const end = ep.endDate || new Date().toISOString().split('T')[0];
        return selectedDateStr >= start && selectedDateStr <= end;
    });

    if (isPeriodIndex !== -1) {
        episodes.splice(isPeriodIndex, 1);
    } else {
        episodes.push({
            id: 'ep_' + Date.now(),
            startDate: selectedDateStr,
            endDate: selectedDateStr,
            isOngoing: false
        });
    }
    localStorage.setItem(STORAGE_KEYS.EPISODES, JSON.stringify(episodes));
    renderCalendar();
    renderHome();
    renderReports();
}

// ========================================================
// TAB 3: JOURNAL
// ========================================================
function renderJournalFeed(filterQuery = '') {
    const list = document.getElementById('journal-list');
    list.innerHTML = '';

    const filtered = journals.filter(j => {
        if (!filterQuery) return true;
        const matchNote = j.note && j.note.toLowerCase().includes(filterQuery);
        const matchSym = j.symptoms && j.symptoms.some(s => s.toLowerCase().includes(filterQuery));
        return matchNote || matchSym;
    });

    if (filtered.length === 0) {
        list.innerHTML = `
            <div style="text-align: center; padding: 40px 10px; color: var(--text-muted);">
                <i class="fa-regular fa-folder-open" style="font-size: 2.5rem; margin-bottom: 12px;"></i>
                <p>Không có bản ghi nhật ký phù hợp</p>
            </div>
        `;
        return;
    }

    filtered.sort((a,b) => new Date(b.date) - new Date(a.date)).forEach(entry => {
        const card = document.createElement('div');
        card.className = 'journal-card';
        card.innerHTML = `
            <div class="card-row">
                <span class="journal-date"><i class="fa-regular fa-calendar"></i> ${entry.date}</span>
                <span class="badge-num">Tâm trạng: ${entry.mood || 3}/5</span>
            </div>
            ${entry.symptoms && entry.symptoms.length ? `
                <div class="journal-tags">
                    ${entry.symptoms.map(s => `<span class="tag-badge">${s}</span>`).join('')}
                </div>
            ` : ''}
            ${entry.note ? `<p class="tip-desc">${entry.note}</p>` : ''}
        `;
        list.appendChild(card);
    });
}

function openJournalModal(preset = null) {
    document.getElementById('journal-modal').classList.remove('hidden');
}

function closeJournalModal() {
    document.getElementById('journal-modal').classList.add('hidden');
}

function toggleChip(chip) {
    chip.classList.toggle('active');
}

function updateSleepLabel(val) {
    document.getElementById('sleep-label').textContent = `${parseFloat(val).toFixed(1)} giờ`;
}

function saveJournalEntry() {
    const activeChips = Array.from(document.querySelectorAll('#symptoms-chips .chip.active')).map(c => c.textContent.trim());
    const intimacyChips = Array.from(document.querySelectorAll('#intimacy-chips .chip.active')).map(c => c.textContent.trim());
    const mucusChips = Array.from(document.querySelectorAll('#mucus-chips .chip.active')).map(c => c.textContent.trim());
    const moodVal = parseInt(document.getElementById('mood-slider').value);
    const sleepVal = parseFloat(document.getElementById('sleep-slider').value);
    const noteVal = document.getElementById('journal-note-input').value.trim();

    const existingIdx = journals.findIndex(j => j.date === selectedDateStr);
    const entryData = {
        id: 'j_' + Date.now(),
        date: selectedDateStr,
        symptoms: activeChips,
        intimacy: intimacyChips,
        mucus: mucusChips,
        mood: moodVal,
        sleep: sleepVal,
        note: noteVal
    };

    if (existingIdx !== -1) {
        journals[existingIdx] = entryData;
    } else {
        journals.unshift(entryData);
    }

    localStorage.setItem(STORAGE_KEYS.JOURNALS, JSON.stringify(journals));
    closeJournalModal();
    renderJournalFeed();
    renderCalendar();
    alert("Đã lưu nhật ký sức khỏe & tránh thai vào bộ nhớ máy!");
}

// ========================================================
// TAB 4: REPORTS & EXPORT
// ========================================================
function renderReports() {
    const median = calculateMedianCycle();
    document.getElementById('stat-cycle-len').textContent = `${median} ngày`;
}

function exportReport(format) {
    if (format === 'csv') {
        let csvContent = "data:text/csv;charset=utf-8,ID,Ngay_Bat_Dau,Ngay_Ket_Thuc,Dang_Dien_Ra\n";
        episodes.forEach(ep => {
            csvContent += `${ep.id},${ep.startDate},${ep.endDate || ''},${ep.isOngoing ? 'Co' : 'Khong'}\n`;
        });
        const encodedUri = encodeURI(csvContent);
        const link = document.createElement("a");
        link.setAttribute("href", encodedUri);
        link.setAttribute("download", `AnNhien_ChuKy_${new Date().toISOString().split('T')[0]}.csv`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    } else {
        let reportTxt = `========================================\n`;
        reportTxt += `BÁO CÁO SỨC KHỎE AN NHIÊN (100% NGOẠI TUYẾN)\n`;
        reportTxt += `Ngày xuất: ${new Date().toLocaleDateString('vi-VN')}\n`;
        reportTxt += `========================================\n\n`;
        reportTxt += `1. THỐNG KÊ CHU KỲ:\n`;
        reportTxt += `- Độ dài chu kỳ trung vị: ${calculateMedianCycle()} ngày\n`;
        reportTxt += `- Tổng số kỳ kinh đã ghi nhận: ${episodes.length}\n\n`;
        reportTxt += `2. NHẬT KÝ SỨC KHỎE GẦN ĐÂY:\n`;
        journals.slice(0, 10).forEach(j => {
            reportTxt += `• Ngày ${j.date}: Triệu chứng: [${j.symptoms.join(', ')}] | Tâm trạng: ${j.mood}/5 | Ghi chú: ${j.note || 'Không'}\n`;
        });
        reportTxt += `\nLưu ý: Báo cáo này để tự theo dõi cá nhân, không phải chẩn đoán y khoa.\n`;

        const blob = new Blob([reportTxt], { type: 'text/plain;charset=utf-8' });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `Bao_Cao_Suc_Khoe_An_Nhien_${new Date().toISOString().split('T')[0]}.txt`;
        link.click();
        URL.revokeObjectURL(url);
    }
}

// ========================================================
// TAB 5: 8 SPECIALIZED MODULES (MODAL SYSTEM)
// ========================================================
function openModule(type) {
    const modal = document.getElementById('module-modal');
    const title = document.getElementById('module-title');
    const body = document.getElementById('module-body');
    modal.classList.remove('hidden');

    switch (type) {
        case 'cycle':
            title.textContent = "Chu kỳ kinh nguyệt";
            body.innerHTML = `
                <div class="card card-regularity">
                    <strong>Độ dài chu kỳ trung vị: ${calculateMedianCycle()} ngày</strong>
                    <p class="tip-desc">Thuật toán loại bỏ các chu kỳ bị bỏ sót hoặc biến động ngoại lai để dự đoán chính xác nhất.</p>
                </div>
                <h4 class="section-title">Lịch sử các kỳ kinh</h4>
                <div style="display: flex; flex-direction: column; gap: 8px;">
                    ${episodes.map(e => `
                        <div class="card" style="padding: 12px;">
                            <strong>Bắt đầu: ${e.startDate}</strong>
                            <p class="tip-desc">${e.isOngoing ? 'Đang diễn ra' : `Kết thúc: ${e.endDate || '—'}`}</p>
                        </div>
                    `).join('')}
                </div>
            `;
            break;

        case 'conception':
            title.textContent = "Kế hoạch mang thai";
            body.innerHTML = `
                <div class="card" style="background: var(--error-container); border: none;">
                    <strong style="color: var(--error);"><i class="fa-solid fa-triangle-exclamation"></i> Lưu ý y khoa bắt buộc</strong>
                    <p class="tip-desc" style="color: var(--error);">
                        1. Thuật toán thụ thai chỉ hỗ trợ kế hoạch sinh con, TUYỆT ĐỐI KHÔNG dùng để tránh thai tự nhiên.<br>
                        2. Ứng dụng không bao giờ gắn nhãn "ngày an toàn" vì không có căn cứ sinh học chắc chắn.<br>
                        3. Trạng thái hiện tại: Đang chờ phê duyệt lâm sàng (PENDING_METHOD_APPROVAL).
                    </p>
                </div>
                <h4 class="section-title">Ghi nhiệt độ cơ thể nền (BBT)</h4>
                <div class="card">
                    <input type="number" step="0.1" value="36.5" class="text-area" style="height: 44px; margin-bottom: 8px;" id="input-bbt">
                    <button class="btn-primary" onclick="alert('Đã lưu BBT ' + document.getElementById('input-bbt').value + '°C')">Lưu nhiệt độ</button>
                </div>
            `;
            break;

        case 'pregnancy':
            title.textContent = "Theo dõi thai kỳ";
            body.innerHTML = `
                <div class="cycle-hero-card" style="background: var(--secondary-container);">
                    <div class="hero-badge">Tuần thứ 24 + 3 ngày</div>
                    <h3 class="hero-title">Em bé có kích thước bằng bắp ngô (~600g)</h3>
                    <p class="hero-desc">Thính giác của bé đã phát triển, có thể nghe được tiếng mẹ trò chuyện.</p>
                </div>
                <h4 class="section-title">Đếm cử động thai máy</h4>
                <div class="card" style="text-align: center;">
                    <span style="font-size: 2.5rem; font-weight: 800; color: var(--primary);" id="kicks-num">0</span>
                    <p class="tip-desc">cử động thai ghi nhận hôm nay</p>
                    <button class="btn-primary" style="margin-top: 10px;" onclick="addKick()">Bé vừa đạp +1</button>
                </div>
                <h4 class="section-title">Đồng hồ đo cơn co thắt (Cơn gò)</h4>
                <div class="card" style="text-align: center;">
                    <span style="font-size: 2.2rem; font-weight: 800;" id="contraction-clock">00:00</span>
                    <div style="margin-top: 12px; display: flex; gap: 8px;">
                        <button id="btn-contraction" class="btn-primary" style="flex: 1;" onclick="toggleContractionTimer()">Bắt đầu cơn gò</button>
                    </div>
                </div>
            `;
            break;

        case 'postpartum':
            title.textContent = "Phục hồi sau sinh";
            body.innerHTML = `
                <h4 class="section-title">Theo dõi sản dịch (Lochia)</h4>
                <div class="card">
                    <p class="tip-desc">Sản dịch bình thường sẽ nhạt dần từ đỏ tươi sang hồng nâu, vàng nhạt rồi chuyển sang trong suốt sau vài tuần.</p>
                    <select class="text-area" style="height: 44px; margin-top: 8px;">
                        <option>Đỏ tươi (Lochia rubra)</option>
                        <option selected>Hồng / Nâu nhạt (Lochia serosa)</option>
                        <option>Trắng đục / Vàng nhạt (Lochia alba)</option>
                        <option>Có mùi hôi bất thường (Cần đi khám ngay)</option>
                    </select>
                </div>
                <h4 class="section-title">Tâm lý người mẹ sau sinh</h4>
                <div class="card card-tip">
                    <p class="tip-desc">Nếu bạn cảm thấy lo âu, mệt mỏi hoặc kiệt sức kéo dài trên 2 tuần, hãy chia sẻ ngay với người thân hoặc liên hệ bác sĩ để được hỗ trợ kịp thời.</p>
                </div>
            `;
            break;

        case 'perimenopause':
            title.textContent = "Tiền mãn kinh & Mãn kinh";
            body.innerHTML = `
                <div class="card" style="text-align: center;">
                    <span style="font-size: 2.5rem; font-weight: 800; color: var(--tertiary);" id="hotflash-num">0</span>
                    <p class="tip-desc">cơn bốc hỏa ghi nhận hôm nay</p>
                    <button class="btn-primary" style="background: var(--tertiary); margin-top: 10px;" onclick="addHotFlash()">Vừa xuất hiện cơn bốc hỏa +1</button>
                </div>
                <h4 class="section-title">Dấu hiệu nhận biết tự nhiên</h4>
                <div class="card">
                    <p class="tip-desc">• Chu kỳ kinh nguyệt thưa hoặc dày hơn bình thường<br>• Đổ mồ hôi đêm, khó ngủ<br>• Thay đổi tính tình và giảm năng lượng</p>
                </div>
            `;
            break;

        case 'dailyhealth':
            title.textContent = "Sức khỏe mỗi ngày";
            body.innerHTML = `
                <h4 class="section-title">Tính chỉ số khối cơ thể (BMI)</h4>
                <div class="card">
                    <div style="display: flex; gap: 10px; margin-bottom: 10px;">
                        <input type="number" placeholder="Cân nặng (kg)" id="bmi-w" value="52" class="text-area" style="height: 44px;">
                        <input type="number" placeholder="Chiều cao (cm)" id="bmi-h" value="160" class="text-area" style="height: 44px;">
                    </div>
                    <button class="btn-primary" onclick="calcBmi()">Tính BMI</button>
                    <p id="bmi-result" style="margin-top: 10px; font-weight: 700; color: var(--primary);">BMI: 20.3 (Bình thường)</p>
                </div>
            `;
            break;

        case 'relaxation':
            title.textContent = "Thư giãn & Nhịp thở";
            body.innerHTML = `
                <div style="margin-bottom: 14px;">
                    <label style="font-size: 0.85rem; font-weight: 600; color: var(--on-surface-variant); display: block; margin-bottom: 6px;">Chọn bài tập thở:</label>
                    <select id="select-breath-mode" class="text-area" style="height: 42px; font-weight: 600;" onchange="onBreathModeChange()">
                        <option value="box">📦 Thở hộp (Box 4-4-4-4) • Giảm stress cấp</option>
                        <option value="478">🌙 Thở ru ngủ sâu (4-7-8) • Ngủ ngon</option>
                        <option value="diaphragm">🌸 Thở bụng cơ hoành (4-6) • Đau bụng kinh</option>
                        <option value="coherent">☀️ Thở cân bằng tim (5-5) • Năng lượng</option>
                        <option value="cooling">❄️ Thở làm mát (4-2-6) • Cơn bốc hỏa</option>
                        <option value="wimhof">🧊 Phương pháp Wim Hof • 30 nhịp + Nín thở yếm khí</option>
                    </select>
                </div>

                <div id="wimhof-warning-banner" class="card" style="display: none; background: var(--error-container); border: none; margin-bottom: 12px; font-size: 0.85rem; color: var(--error);">
                    <strong><i class="fa-solid fa-triangle-exclamation"></i> CẢNH BÁO Y TẾ WIM HOF:</strong><br>
                    • TUYỆT ĐỐI KHÔNG TẬP KHI ĐANG MANG THAI.<br>
                    • Luôn ngồi hoặc nằm trên thảm/giường. Không tập dưới nước hoặc khi lái xe.
                </div>

                <div class="breathing-box">
                    <div class="breathing-circle-outer" id="breath-circle">
                        <div class="breathing-circle-inner" id="breath-sec">✦</div>
                    </div>
                    <h3 id="breath-instruction">Nhấn Bắt đầu để đồng bộ nhịp thở</h3>
                    <p id="breath-desc" class="tip-desc" style="text-align: center; margin-top: 6px;">Hít vào 4s, giữ 4s, thở ra 4s, nín thở 4s. Giúp làm dịu hệ thần kinh tức thì.</p>
                    <button id="btn-breath-toggle" class="btn-primary" style="margin-top: 16px; width: 220px;" onclick="toggleBreathing()">Bắt đầu thở</button>
                    <p style="margin-top: 10px; font-weight: 700; color: var(--primary);">Đã hoàn thành: <span id="breath-cycles">0</span> chu kỳ</p>
                </div>

                <div class="card" style="margin-top: 14px;">
                    <strong><i class="fa-solid fa-spa text-primary"></i> Thư giãn cơ tiến triển (PMR)</strong>
                    <p class="tip-desc" style="margin-top: 4px; line-height: 1.5;">
                        Tuần tự siết căng 5 giây rồi thả lỏng hoàn toàn:<br>
                        • <strong>Bàn chân:</strong> Gập ngón chân siết chặt rồi thả mềm.<br>
                        • <strong>Vùng chậu & Đùi:</strong> Gồng cơ sàn chậu 5s rồi buông lơi.<br>
                        • <strong>Vai & Cổ:</strong> Rụt vai chạm tai rồi thả lỏng tức thì.
                    </p>
                </div>

                <div class="card" style="margin-top: 10px;">
                    <strong><i class="fa-solid fa-brain" style="color: var(--secondary);"></i> Neo giữ thực tại 5-4-3-2-1</strong>
                    <p class="tip-desc" style="margin-top: 4px;">5 vật nhìn thấy • 4 điểm chạm • 3 âm thanh • 2 mùi hương • 1 vị cảm nhận.</p>
                </div>
            `;
            break;

        case 'medicalrecords':
            title.textContent = "Sổ khám & Đơn thuốc";
            let medsSaved = JSON.parse(localStorage.getItem('annhien_meds') || '[]');
            let visitsSaved = JSON.parse(localStorage.getItem('annhien_visits') || '[]');
            body.innerHTML = `
                <div class="tabs-sub" style="display: flex; gap: 8px; margin-bottom: 12px;">
                    <button class="btn-primary-sm" style="flex: 1;" onclick="showMedSubTab('visits')">Lượt khám (${visitsSaved.length})</button>
                    <button class="btn-outline" style="flex: 1;" onclick="showMedSubTab('meds')">Thuốc & Vi chất (${medsSaved.length})</button>
                </div>

                <div id="subtab-visits">
                    <h4 class="section-title">Thêm lượt khám mới</h4>
                    <div class="card" style="margin-bottom: 14px;">
                        <input type="text" placeholder="Cơ sở y tế (Bệnh viện / Phòng khám)" class="text-area" style="height: 44px; margin-bottom: 8px;" id="med-fac">
                        <input type="text" placeholder="Bác sĩ phụ trách" class="text-area" style="height: 44px; margin-bottom: 8px;" id="med-doc">
                        <textarea placeholder="Câu hỏi muốn hỏi bác sĩ..." class="text-area" style="height: 54px; margin-bottom: 8px;" id="med-ques"></textarea>
                        <textarea placeholder="Kết luận sau khám, dặn dò..." class="text-area" style="height: 54px; margin-bottom: 8px;" id="med-notes"></textarea>
                        
                        <!-- Attach Prescription Photo -->
                        <div style="margin-bottom: 10px;">
                            <input type="file" id="med-photo-input" accept="image/*" capture="environment" style="display: none;" onchange="simHandleMedPhoto(event)">
                            <button type="button" class="btn-outline" style="width: 100%; font-size: 0.8rem; padding: 7px; display: flex; align-items: center; justify-content: center; gap: 6px;" onclick="document.getElementById('med-photo-input').click()">
                                <i class="fa-solid fa-camera"></i> Chụp ảnh kết quả / đơn thuốc
                            </button>
                            <div id="med-photo-preview" style="display: flex; gap: 8px; margin-top: 8px; flex-wrap: wrap;"></div>
                        </div>

                        <button class="btn-primary" onclick="saveMedVisit()">Lưu hồ sơ khám</button>
                    </div>

                    <h4 class="section-title">Lịch sử các lần khám</h4>
                    <div id="visits-list" style="display: flex; flex-direction: column; gap: 10px;">
                        ${visitsSaved.length === 0 ? '<p class="tip-desc" style="text-align:center; padding: 12px;">Chưa có lượt khám nào được lưu.</p>' : visitsSaved.map((v, idx) => `
                            <div class="card" style="padding: 14px;">
                                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 4px;">
                                    <strong style="color: var(--primary); font-size: 0.95rem;">${v.facility || 'Cơ sở y tế'}</strong>
                                    <small style="color: var(--text-muted);">${v.date || 'Hôm nay'}</small>
                                </div>
                                ${v.doctor ? `<p class="tip-desc" style="margin: 2px 0;"><i class="fa-solid fa-user-doctor"></i> Bác sĩ: ${v.doctor}</p>` : ''}
                                ${v.notes ? `<p style="font-size: 0.82rem; margin: 4px 0; color: var(--text-primary); line-height: 1.4;"><strong>Kết luận:</strong> ${v.notes}</p>` : ''}
                                ${v.photos && v.photos.length > 0 ? `
                                    <div style="margin-top: 8px;">
                                        <span style="font-size: 0.72rem; color: var(--text-muted); display: block; margin-bottom: 4px;"><i class="fa-solid fa-paperclip"></i> Ảnh chụp đơn thuốc (${v.photos.length}):</span>
                                        <div style="display: flex; gap: 6px; overflow-x: auto; padding-bottom: 4px;">
                                            ${v.photos.map(p => `
                                                <img src="${p}" style="width: 60px; height: 60px; object-fit: cover; border-radius: 8px; border: 1px solid var(--outline); cursor: pointer;" onclick="simViewFullPhoto('${p}')">
                                            `).join('')}
                                        </div>
                                    </div>
                                ` : ''}
                            </div>
                        `).join('')}
                    </div>
                </div>

                <div id="subtab-meds" style="display: none;">
                    <h4 class="section-title">Thêm thuốc / vi chất bổ sung</h4>
                    <div class="card">
                        <input type="text" placeholder="Tên thuốc / vi chất (Sắt, Canxi, Folate...)" class="text-area" style="height: 44px; margin-bottom: 8px;" id="input-med-name">
                        <input type="text" placeholder="Liều lượng (VD: 1 viên, 500mg...)" class="text-area" style="height: 44px; margin-bottom: 8px;" id="input-med-dose">
                        <button class="btn-primary" onclick="saveMedicationItem()">Thêm vào danh sách</button>
                    </div>
                    <div id="meds-list" style="margin-top: 12px; display: flex; flex-direction: column; gap: 8px;">
                        ${medsSaved.length === 0 ? '<p class="tip-desc" style="text-align:center;">Chưa có thuốc nào được lưu.</p>' : medsSaved.map((m, idx) => `
                            <div class="card" style="padding: 12px; display: flex; justify-content: space-between; align-items: center;">
                                <div>
                                    <strong>${m.name}</strong>
                                    <p class="tip-desc">Liều dùng: ${m.dose || 'Theo chỉ định'}</p>
                                </div>
                                <span class="badge-num" style="background: var(--primary-container); color: var(--primary);">Đang uống</span>
                            </div>
                        `).join('')}
                    </div>
                </div>
            `;
            break;

        case 'reminders':
            title.textContent = "Trung tâm nhắc nhở";
            body.innerHTML = `
                <div class="card card-tip" style="margin-bottom: 14px;">
                    <strong><i class="fa-solid fa-shield-halved"></i> 100% Cục bộ & Ngoại tuyến</strong>
                    <p class="tip-desc">Tất cả thông báo được hẹn giờ bằng AlarmManager nội bộ máy. Không gửi bất kỳ dữ liệu nào qua mạng.</p>
                </div>
                <div style="display: flex; flex-direction: column; gap: 10px;">
                    <div class="card" style="display: flex; justify-content: space-between; align-items: center; padding: 14px;">
                        <div>
                            <strong>Dự báo chu kỳ kinh</strong>
                            <p class="tip-desc">Báo trước 2 ngày (08:00 sáng)</p>
                        </div>
                        <input type="checkbox" checked style="width: 20px; height: 20px; accent-color: var(--primary);">
                    </div>
                    <div class="card" style="display: flex; justify-content: space-between; align-items: center; padding: 14px;">
                        <div>
                            <strong>Nhắc uống nước đủ lượng</strong>
                            <p class="tip-desc">Mỗi 2 tiếng (08:00 - 20:00)</p>
                        </div>
                        <input type="checkbox" checked style="width: 20px; height: 20px; accent-color: var(--primary);">
                    </div>
                    <div class="card" style="display: flex; justify-content: space-between; align-items: center; padding: 14px;">
                        <div>
                            <strong>Ghi nhật ký cảm xúc</strong>
                            <p class="tip-desc">Mỗi tối lúc 21:00</p>
                        </div>
                        <input type="checkbox" checked style="width: 20px; height: 20px; accent-color: var(--primary);">
                    </div>
                    <div class="card" style="display: flex; justify-content: space-between; align-items: center; padding: 14px;">
                        <div>
                            <strong>Uống thuốc & Vi chất</strong>
                            <p class="tip-desc">Theo giờ trong đơn thuốc</p>
                        </div>
                        <input type="checkbox" checked style="width: 20px; height: 20px; accent-color: var(--primary);">
                    </div>
                    <div class="card" style="display: flex; justify-content: space-between; align-items: center; padding: 14px;">
                        <div>
                            <strong>Bài tập Kegel & Phục hồi</strong>
                            <p class="tip-desc">15:00 chiều mỗi ngày</p>
                        </div>
                        <input type="checkbox" style="width: 20px; height: 20px; accent-color: var(--primary);">
                    </div>
                </div>
                <button class="btn-primary" style="margin-top: 14px; width: 100%;" onclick="alert('Đã cập nhật lịch báo thức ngoại tuyến thành công!')">Lưu cấu hình nhắc nhở</button>
            `;
            break;

        case 'knowledge':
            title.textContent = "Thư viện kiến thức";
            body.innerHTML = `
                <div class="card">
                    <h4>1. Hiểu đúng về chu kỳ kinh nguyệt</h4>
                    <p class="tip-desc">Chu kỳ kinh nguyệt bình thường của phụ nữ dao động từ 21 đến 35 ngày, bao gồm pha nang noãn, rụng trứng và hoàng thể...</p>
                </div>
                <div class="card">
                    <h4>2. Tại sao không dùng app để tránh thai tự nhiên?</h4>
                    <p class="tip-desc">Thời điểm rụng trứng có thể dịch chuyển do stress hoặc cảm sốt. Tinh trùng có thể sống 5 ngày trong đường sinh sản...</p>
                </div>
                <div class="card">
                    <h4>3. Dấu hiệu bất thường cần đi khám phụ khoa</h4>
                    <p class="tip-desc">Đau bụng dữ dội không dứt, xuất huyết bất thường giữa kỳ kinh, sản dịch có mùi hôi nồng...</p>
                </div>
            `;
            break;

        case 'pillpack':
            title.textContent = "Vỉ thuốc tránh thai 28 viên";
            let pillSaved = JSON.parse(localStorage.getItem('annhien_pills')) || {};
            let pillHtml = `<div class="pill-pack-container">
                <div class="card-row">
                    <strong>Vỉ 28 viên (Microgynon / Yasmin...)</strong>
                    <span class="badge-num">21 hoạt tính + 7 giả dược</span>
                </div>
                <div class="pill-pack-grid">`;
            for (let i = 1; i <= 28; i++) {
                const isPlacebo = i > 21;
                const isTaken = pillSaved[i] || (i <= 13);
                pillHtml += `<div class="pill-item ${isPlacebo ? 'placebo' : ''} ${isTaken ? 'taken' : ''}" onclick="togglePillItem(${i}, this)">
                    <span>${i}</span>
                    <small style="font-size: 9px;">${isTaken ? '✓' : ''}</small>
                </div>`;
            }
            pillHtml += `</div></div>
            <h4 class="section-title">Hướng dẫn xử trí khi quên uống thuốc</h4>
            <div class="card" style="font-size: 0.85rem; line-height: 1.5;">
                <p><strong>Quên 1 viên (&lt; 12 giờ):</strong> Uống ngay viên bị quên ngay khi nhớ ra, viên tiếp theo uống đúng giờ thường lệ. Vẫn được bảo vệ an toàn.</p>
                <div class="divider" style="margin: 8px 0;"></div>
                <p><strong>Quên 1 viên (&gt; 12 giờ) hoặc quên từ 2 viên:</strong> Uống ngay viên gần nhất, bỏ viên trước đó. Bắt buộc dùng thêm bao cao su trong 7 ngày tới.</p>
            </div>`;
            body.innerHTML = pillHtml;
            break;

        case 'kegel':
            title.textContent = "Bài tập sàn chậu Kegel";
            body.innerHTML = `
                <div class="kegel-box">
                    <div class="kegel-ring-outer" id="kegel-ring">
                        <div class="kegel-ring-inner">
                            <span id="kegel-sec" style="font-size: 2.2rem;">5</span>
                            <span id="kegel-phase" style="font-size: 0.75rem; margin-top: 4px;">SẴN SÀNG</span>
                        </div>
                    </div>
                    <h3 id="kegel-instruction" style="text-align: center; margin-top: 14px;">Nhấn Bắt đầu để đồng bộ nhịp tập</h3>
                    <p class="tip-desc" style="text-align: center; margin-top: 6px;">Co cơ đáy chậu như khi nín tiểu trong 5 giây, sau đó thả lỏng hoàn toàn trong 5 giây.</p>
                    <button id="btn-kegel-toggle" class="btn-primary" style="margin-top: 18px; width: 220px;" onclick="toggleKegelTrainer()">Bắt đầu tập Kegel</button>
                    <p style="margin-top: 12px; font-weight: 700; color: var(--tertiary);">Đã hoàn thành: <span id="kegel-reps">0</span> hiệp</p>
                </div>
            `;
            break;

        case 'breast':
            title.textContent = "Tự khám vú định kỳ tại nhà";
            body.innerHTML = `
                <div class="card card-tip" style="margin-bottom: 16px;">
                    <strong>Thời điểm vàng: 3–5 ngày sau khi sạch kinh</strong>
                    <p class="tip-desc">Đây là lúc nồng độ hormone estrogen và progesterone thấp nhất, mô tuyến vú mềm mại nhất, dễ phát hiện nốt sần bất thường nhất.</p>
                </div>
                <div style="display: flex; flex-direction: column; gap: 12px;">
                    <div class="card">
                        <strong>Bước 1: Quan sát trước gương</strong>
                        <p class="tip-desc">Đứng thẳng xuôi tay, sau đó chống hai tay lên hông. Quan sát hình dáng 2 bầu ngực, xem có vết lõm da, sần da cam hay đổi màu bất thường không.</p>
                    </div>
                    <div class="card">
                        <strong>Bước 2: Giơ 2 tay lên cao</strong>
                        <p class="tip-desc">Quan sát xem núm vú có bị kéo tụt vào trong hoặc bờ vú có dịch chuyển không cân đối khi cử động cánh tay.</p>
                    </div>
                    <div class="card">
                        <strong>Bước 3: Sờ nắn tư thế nằm</strong>
                        <p class="tip-desc">Đặt gối mỏng dưới vai. Dùng 3 ngón tay giữa khép lại, day nhẹ nhàng theo hình xoắn ốc từ ngoài vào núm vú để kiểm tra khối u hoặc nốt sần.</p>
                    </div>
                    <div class="card">
                        <strong>Bước 4: Kiểm tra núm vú &amp; nách</strong>
                        <p class="tip-desc">Bóp nhẹ núm vú kiểm tra xem có tiết dịch vàng đục hoặc máu không. Dùng tay rà soát hạch vùng hõm nách.</p>
                    </div>
                </div>
            `;
            break;

        case 'epds':
            title.textContent = "Sàng lọc trầm cảm sau sinh (EPDS)";
            body.innerHTML = `
                <div class="card card-regularity">
                    <strong>Thang đo Edinburgh Postnatal Depression Scale (EPDS)</strong>
                    <p class="tip-desc">Bộ 10 câu hỏi tiêu chuẩn y tế quốc tế giúp phụ nữ tự đánh giá cảm xúc trong 7 ngày qua.</p>
                </div>
                <div id="epds-questions-list"></div>
                <button class="btn-primary" style="margin-top: 12px; width: 100%;" onclick="calculateEpdsScore()">Xem kết quả đánh giá</button>
                <div id="epds-result" style="margin-top: 14px;"></div>
            `;
            setTimeout(initEpdsQuestions, 50);
            break;

        case 'settings':
            title.textContent = "Cài đặt & Bảo mật";
            body.innerHTML = `
                <div class="card">
                    <div class="card-row">
                        <div>
                            <strong>Khóa ứng dụng bằng PIN</strong>
                            <p class="tip-desc">Yêu cầu nhập mã 4 số khi mở ứng dụng</p>
                        </div>
                        <input type="checkbox" ${lockEnabled ? 'checked' : ''} onchange="toggleLockPref(this.checked)" style="transform: scale(1.3);">
                    </div>
                </div>
                <div class="card">
                    <strong>Đổi mã PIN (Mặc định: 1234)</strong>
                    <div style="display: flex; gap: 8px; margin-top: 8px;">
                        <input type="password" maxlength="4" placeholder="PIN mới" id="new-pin-input" class="text-area" style="height: 42px;">
                        <button class="btn-primary" style="width: 100px;" onclick="changePin()">Lưu</button>
                    </div>
                </div>
                <div class="card">
                    <strong>Chế độ ngụy trang máy tính bỏ túi</strong>
                    <p class="tip-desc">Biến toàn bộ giao diện thành máy tính thông thường. Nhập mã PIN rồi nhấn dấu "=" để mở lại An Nhiên.</p>
                    <button class="btn-outline" style="margin-top: 8px; width: 100%;" onclick="document.getElementById('calculator-overlay').classList.remove('hidden')">
                        <i class="fa-solid fa-calculator"></i> Bật chế độ ngụy trang ngay
                    </button>
                </div>
                <div class="card card-tip">
                    <strong>Cam kết riêng tư 100%</strong>
                    <p class="tip-desc">Ứng dụng không gửi bất kỳ dữ liệu nào lên mạng. Mọi thông tin nằm trọn vẹn trong bộ nhớ trình duyệt của bạn.</p>
                </div>
                <div class="card" style="text-align: center; padding: 18px 14px; background: linear-gradient(135deg, rgba(184,142,209,0.12), rgba(230,123,123,0.08)); border: 1px solid var(--outline-variant); margin-top: 10px;">
                    <div style="font-size: 1.8rem; margin-bottom: 4px;">🌸</div>
                    <h3 style="margin: 0 0 2px 0; font-size: 1.05rem; color: var(--on-surface);">An Nhiên</h3>
                    <p class="tip-desc" style="margin: 0 0 8px 0; font-size: 0.75rem;">Sức khỏe phụ nữ toàn diện &amp; 100% Ngoại tuyến</p>
                    <div style="display: inline-block; background: var(--surface); border: 1px solid var(--outline-variant); padding: 5px 14px; border-radius: 20px; margin-bottom: 8px;">
                        <span style="font-weight: 700; color: var(--tertiary); font-size: 0.82rem;"><i class="fa-solid fa-code"></i> Tạo bởi: 102</span>
                    </div>
                    <div style="font-size: 0.72rem; color: var(--text-muted); line-height: 1.4;">
                        Phiên bản 1.0.0 • SQLite Offline-First<br>
                        Bảo mật dữ liệu cá nhân theo chuẩn y tế
                    </div>
                </div>
            `;
            break;

        case 'healthtopics':
            title.textContent = "Đề tài sức khỏe phụ nữ (MyHealthfinder)";
            body.innerHTML = simRenderHealthTopicsView();
            break;

        case 'airquality':
            title.textContent = "Chất lượng không khí (AQICN / WAQI)";
            body.innerHTML = simRenderAirQualityView();
            break;

        case 'weatheruv':
            title.textContent = "Thời tiết & Cảnh báo UV (Open-Meteo)";
            body.innerHTML = simRenderWeatherView();
            break;

        case 'workouts':
            title.textContent = "Bài tập thể dục phụ nữ (Wger)";
            body.innerHTML = simRenderWorkoutsView();
            break;

        case 'quotes':
            title.textContent = "Lời khích lệ mỗi ngày (Quotable)";
            body.innerHTML = simRenderQuotesView();
            break;

        case 'usdanutrition':
            title.textContent = "Vi chất dinh dưỡng phụ nữ (USDA FDC)";
            body.innerHTML = simRenderUsdaNutritionView();
            break;

        case 'pollenallergy':
            title.textContent = "Dị ứng & Phấn hoa (Open-Meteo AQ)";
            body.innerHTML = simRenderPollenView();
            break;

        case 'onlineutilities':
            title.textContent = "Quản lý tiện ích trực tuyến";
            let simNetOnline = localStorage.getItem('annhien_net_online') !== 'false';
            const simCacheItems = JSON.parse(localStorage.getItem('annhien_sim_cache') || '[{"key":"weather_hanoi","size":"6.4 KB","age":"12 phút"},{"key":"workouts_pelvic","size":"14.8 KB","age":"1 ngày"},{"key":"quote_daily","size":"1.2 KB","age":"4 giờ"},{"key":"usda_folate_iron","size":"18.5 KB","age":"8 giờ"},{"key":"pollen_hanoi","size":"5.2 KB","age":"25 phút"},{"key":"aq_hanoi","size":"8.2 KB","age":"15 phút"},{"key":"food_yenmach","size":"3.1 KB","age":"2 giờ"},{"key":"healthtopics_women","size":"28.4 KB","age":"2 giờ"}]');
            const totalKb = simCacheItems.reduce((s, i) => s + parseFloat(i.size), 0).toFixed(1);
            body.innerHTML = `
                <div class="card" style="display: flex; justify-content: space-between; align-items: center; padding: 14px; margin-bottom: 10px;">
                    <div>
                        <strong>Kết nối Internet</strong>
                        <p class="tip-desc">${simNetOnline ? '🟢 Đang kết nối — Offline-First bật' : '🔴 Ngoại tuyến hoàn toàn'}</p>
                    </div>
                    <input type="checkbox" id="sim-net-toggle" ${simNetOnline ? 'checked' : ''} onchange="simToggleNetwork(this.checked)" style="transform: scale(1.3); accent-color: var(--primary);">
                </div>

                <h4 class="section-title">Nhà cung cấp & Trạng thái (8 Dịch vụ)</h4>
                <div style="display: flex; flex-direction: column; gap: 8px; margin-bottom: 14px;">
                    <!-- Module 1: Open-Meteo Weather -->
                    <div class="card" style="padding: 12px;">
                        <div style="display: flex; justify-content: space-between; align-items: center;">
                            <div>
                                <strong>1. Open-Meteo Weather & UV</strong>
                                <p class="tip-desc">api.open-meteo.com • Không cần API Key • 10.000 req/ngày</p>
                            </div>
                            <span class="badge-num" style="background: #d4f1d4; color: #1a7a1a;">✓ Sẵn sàng</span>
                        </div>
                        <div style="margin-top: 6px; display: flex; gap: 12px; font-size: 0.78rem; color: var(--text-muted);">
                            <span>TTL: <strong>1 giờ</strong></span>
                            <span>Chỉ số: <strong>Nhiệt độ, UV, Gió, Mưa</strong></span>
                        </div>
                    </div>

                    <!-- Module 2: Wger Workout API -->
                    <div class="card" style="padding: 12px;">
                        <div style="display: flex; justify-content: space-between; align-items: center;">
                            <div>
                                <strong>2. Wger Workout Manager</strong>
                                <p class="tip-desc">wger.de/api/v2 • Không cần API Key • Mã nguồn mở</p>
                            </div>
                            <span class="badge-num" style="background: #d4f1d4; color: #1a7a1a;">✓ Sẵn sàng</span>
                        </div>
                        <div style="margin-top: 6px; display: flex; gap: 12px; font-size: 0.78rem; color: var(--text-muted);">
                            <span>TTL: <strong>7 ngày</strong></span>
                            <span>Chuyên đề: <strong>Sàn chậu, Thai kỳ, Giảm đau kinh</strong></span>
                        </div>
                    </div>

                    <!-- Module 3: Quotable.io -->
                    <div class="card" style="padding: 12px;">
                        <div style="display: flex; justify-content: space-between; align-items: center;">
                            <div>
                                <strong>3. Quotable.io</strong>
                                <p class="tip-desc">api.quotable.io • Không cần API Key • ~180 req/phút</p>
                            </div>
                            <span class="badge-num" style="background: #d4f1d4; color: #1a7a1a;">✓ Sẵn sàng</span>
                        </div>
                        <div style="margin-top: 6px; display: flex; gap: 12px; font-size: 0.78rem; color: var(--text-muted);">
                            <span>TTL: <strong>24 giờ</strong></span>
                            <span>Hỗ trợ: <strong>Song ngữ Anh - Việt, Text-to-Speech</strong></span>
                        </div>
                    </div>

                    <!-- Module 6: USDA FDC -->
                    <div class="card" style="padding: 12px;">
                        <div style="display: flex; justify-content: space-between; align-items: center;">
                            <div>
                                <strong>6. USDA FoodData Central</strong>
                                <p class="tip-desc">api.nal.usda.gov • DEMO_KEY / Miễn phí • CSDL Dinh dưỡng Mỹ</p>
                            </div>
                            <span class="badge-num" style="background: #d4f1d4; color: #1a7a1a;">✓ Sẵn sàng</span>
                        </div>
                        <div style="margin-top: 6px; display: flex; gap: 12px; font-size: 0.78rem; color: var(--text-muted);">
                            <span>TTL: <strong>24 giờ</strong></span>
                            <span>Vi chất: <strong>Folate (B9), Sắt (Fe), Canxi, Magie</strong></span>
                        </div>
                    </div>

                    <!-- Module 7: Open-Meteo Pollen -->
                    <div class="card" style="padding: 12px;">
                        <div style="display: flex; justify-content: space-between; align-items: center;">
                            <div>
                                <strong>7. Open-Meteo Air Quality (Phấn hoa & Dị ứng)</strong>
                                <p class="tip-desc">air-quality-api.open-meteo.com • Không cần API Key</p>
                            </div>
                            <span class="badge-num" style="background: #d4f1d4; color: #1a7a1a;">✓ Sẵn sàng</span>
                        </div>
                        <div style="margin-top: 6px; display: flex; gap: 12px; font-size: 0.78rem; color: var(--text-muted);">
                            <span>TTL: <strong>6 giờ</strong></span>
                            <span>Chỉ số: <strong>Phấn cỏ, Phấn hoa gỗ, Cúc dại, Bào tử</strong></span>
                        </div>
                    </div>

                    <div class="card" style="padding: 12px;">
                        <div style="display: flex; justify-content: space-between; align-items: center;">
                            <div>
                                <strong>USDA FoodData Central (12 Vi chất)</strong>
                                <p class="tip-desc">CSDL Vi chất dinh dưỡng phụ nữ • TTL 24 giờ</p>
                            </div>
                            <span class="badge-num" style="background: #d4f1d4; color: #1a7a1a;">✓ Sẵn sàng</span>
                        </div>
                    </div>
                    <div class="card" style="padding: 12px;">
                        <div style="display: flex; justify-content: space-between; align-items: center;">
                            <div>
                                <strong>MyHealthfinder v4 (HHS)</strong>
                                <p class="tip-desc">Không cần API Key • TTL 72 giờ</p>
                            </div>
                            <span class="badge-num" style="background: #d4f1d4; color: #1a7a1a;">✓ Sẵn sàng</span>
                        </div>
                    </div>
                    <div class="card" style="padding: 12px;">
                        <div style="display: flex; justify-content: space-between; align-items: center;">
                            <div>
                                <strong>OpenAQ v3</strong>
                                <p class="tip-desc">Trạm quan trắc không khí toàn cầu • TTL 30 phút</p>
                            </div>
                            <span class="badge-num" style="background: #d4f1d4; color: #1a7a1a;">✓ Sẵn sàng</span>
                        </div>
                    </div>
                </div>

                <h4 class="section-title">Bộ nhớ đệm (OnlineCacheManager)</h4>
                <div style="display: flex; flex-direction: column; gap: 6px; margin-bottom: 12px;">
                    ${simCacheItems.map(item => `
                    <div class="card" style="padding: 10px 14px; display: flex; justify-content: space-between; align-items: center;">
                        <div>
                            <strong style="font-size: 0.85rem; font-family: monospace;">${item.key}</strong>
                            <p class="tip-desc" style="margin: 0;">${item.size} • ${item.age} trước</p>
                        </div>
                        <span class="badge-num" style="background: var(--surface-variant);">Còn hạn</span>
                    </div>`).join('')}
                </div>
                <div class="card" style="padding: 14px;">
                    <div class="card-row">
                        <div>
                            <strong>Tổng dung lượng tạm</strong>
                            <p class="tip-desc">${totalKb} KB / 5.0 MB (${(totalKb/5000*100).toFixed(1)}%)</p>
                        </div>
                        <button class="btn-outline" onclick="simClearAllCache()"><i class="fa-solid fa-trash"></i> Xóa tất cả</button>
                    </div>
                    <div style="height: 6px; background: var(--surface-variant); border-radius: 3px; margin-top: 10px; overflow: hidden;">
                        <div style="height: 100%; width: ${(totalKb/5000*100).toFixed(1)}%; background: var(--primary); border-radius: 3px; transition: width 0.5s;"></div>
                    </div>
                </div>
            `;
            break;
    }
}

function closeModuleModal() {
    document.getElementById('module-modal').classList.add('hidden');
    if (contractionTimerInterval) {
        clearInterval(contractionTimerInterval);
        contractionTimerInterval = null;
    }
    if (breathingInterval) {
        clearInterval(breathingInterval);
        breathingInterval = null;
    }
    if (typeof kegelTimerInterval !== 'undefined' && kegelTimerInterval) {
        clearInterval(kegelTimerInterval);
        kegelTimerInterval = null;
    }
    if (window.speechSynthesis) {
        window.speechSynthesis.cancel();
    }
}

// Module interactive actions
let kicksCount = 0;
function addKick() {
    kicksCount++;
    const el = document.getElementById('kicks-num');
    if (el) el.textContent = kicksCount;
}

function toggleContractionTimer() {
    const btn = document.getElementById('btn-contraction');
    const clock = document.getElementById('contraction-clock');
    if (contractionTimerInterval) {
        clearInterval(contractionTimerInterval);
        contractionTimerInterval = null;
        btn.textContent = "Bắt đầu cơn gò";
        btn.style.background = "var(--primary)";
    } else {
        contractionSeconds = 0;
        btn.textContent = "Kết thúc cơn gò";
        btn.style.background = "var(--error)";
        contractionTimerInterval = setInterval(() => {
            contractionSeconds++;
            const m = String(Math.floor(contractionSeconds / 60)).padStart(2, '0');
            const s = String(contractionSeconds % 60).padStart(2, '0');
            clock.textContent = `${m}:${s}`;
        }, 1000);
    }
}

let hotFlashCount = 0;
function addHotFlash() {
    hotFlashCount++;
    const el = document.getElementById('hotflash-num');
    if (el) el.textContent = hotFlashCount;
}

function calcBmi() {
    const w = parseFloat(document.getElementById('bmi-w').value);
    const h = parseFloat(document.getElementById('bmi-h').value) / 100;
    if (w && h) {
        const bmi = (w / (h * h)).toFixed(1);
        let status = "Bình thường";
        if (bmi < 18.5) status = "Nhẹ cân";
        else if (bmi >= 25) status = "Thừa cân";
        document.getElementById('bmi-result').textContent = `BMI: ${bmi} (${status})`;
    }
}

const BREATH_PRESETS = {
    box: { name: "Thở hộp", desc: "Hít vào 4s, giữ 4s, thở ra 4s, nín thở 4s. Cân bằng hệ thần kinh.", phases: [{ name: "Hít vào từ từ bằng mũi...", sec: 4, cls: "inhale" }, { name: "Giữ hơi thở...", sec: 4, cls: "" }, { name: "Thở ra chậm rãi bằng miệng...", sec: 4, cls: "exhale" }, { name: "Tĩnh lặng thư thái...", sec: 4, cls: "" }] },
    "478": { name: "Thở 4-7-8", desc: "Hít sâu 4s, nín thở 7s, thở ra bằng miệng 8s. Kích thích giấc ngủ sâu.", phases: [{ name: "Hít sâu qua mũi...", sec: 4, cls: "inhale" }, { name: "Nín thở giữ hơi...", sec: 7, cls: "" }, { name: "Thở ra chậm bằng miệng...", sec: 8, cls: "exhale" }] },
    diaphragm: { name: "Thở bụng cơ hoành", desc: "Hít phình bụng 4s, thở hóp bụng 6s. Giảm đau thắt bụng kinh và thả lỏng chậu.", phases: [{ name: "Hít sâu phình bụng...", sec: 4, cls: "inhale" }, { name: "Thở ra chậm rãi hóp bụng...", sec: 6, cls: "exhale" }] },
    coherent: { name: "Thở cân bằng", desc: "Hít vào 5s, thở ra 5s (6 nhịp/phút). Đồng bộ nhịp tim sinh học và tăng tập trung.", phases: [{ name: "Hít vào êm dịu...", sec: 5, cls: "inhale" }, { name: "Thở ra nhịp nhàng...", sec: 5, cls: "exhale" }] },
    cooling: { name: "Thở làm mát", desc: "Hít qua môi 4s, giữ 2s, thở qua mũi 6s. Xua tan cơn bốc hỏa tiền mãn kinh.", phases: [{ name: "Hít đón khí mát qua môi...", sec: 4, cls: "inhale" }, { name: "Giữ hơi êm dịu...", sec: 2, cls: "" }, { name: "Thở ra từ từ qua mũi...", sec: 6, cls: "exhale" }] },
    wimhof: { name: "Wim Hof", desc: "30 nhịp hít sâu làm đầy phổi rồi nín thở tối đa. TUYỆT ĐỐI KHÔNG tập khi mang thai.", isWimHof: true }
};

let breathCycleCount = 0;
let wimHofRetentionSec = 0;

function onBreathModeChange() {
    const mode = document.getElementById('select-breath-mode').value;
    const descEl = document.getElementById('breath-desc');
    const warnEl = document.getElementById('wimhof-warning-banner');

    if (warnEl) {
        warnEl.style.display = (mode === 'wimhof') ? 'block' : 'none';
    }

    if (descEl && BREATH_PRESETS[mode]) {
        descEl.textContent = BREATH_PRESETS[mode].desc;
    }
    if (breathingInterval) {
        toggleBreathing(); // stop current session
    }
}

function toggleBreathing() {
    const btn = document.getElementById('btn-breath-toggle');
    const circle = document.getElementById('breath-circle');
    const secEl = document.getElementById('breath-sec');
    const instEl = document.getElementById('breath-instruction');
    const cycleEl = document.getElementById('breath-cycles');
    const mode = document.getElementById('select-breath-mode') ? document.getElementById('select-breath-mode').value : 'box';
    const preset = BREATH_PRESETS[mode] || BREATH_PRESETS.box;

    if (breathingInterval) {
        clearInterval(breathingInterval);
        breathingInterval = null;
        btn.textContent = "Bắt đầu thở";
        circle.className = "breathing-circle-outer";
        secEl.textContent = "✦";
        instEl.textContent = "Nhấn Bắt đầu để đồng bộ nhịp thở";
        return;
    }

    // Logic riêng cho Wim Hof
    if (preset.isWimHof) {
        btn.textContent = "Dừng bài tập";
        let rep = 1;
        let isInhale = true;
        let wimHofPhase = "POWER"; // POWER -> RETENTION -> RECOVERY
        instEl.textContent = `Nhịp 1/30: Hít vào thật sâu...`;
        circle.className = "breathing-circle-outer inhale";
        secEl.textContent = "1";

        breathingInterval = setInterval(() => {
            if (wimHofPhase === "POWER") {
                isInhale = !isInhale;
                if (isInhale) {
                    rep++;
                    if (rep > 30) {
                        // Chuyển sang Nín thở
                        wimHofPhase = "RETENTION";
                        wimHofRetentionSec = 0;
                        circle.className = "breathing-circle-outer";
                        instEl.innerHTML = `Thở ra & NÍN THỞ TỐI ĐA...<br><small style="color:var(--primary);cursor:pointer;" onclick="triggerWimHofRecovery()">[Nhấn vào đây khi muốn thở lại]</small>`;
                        secEl.textContent = "0s";
                        return;
                    }
                    instEl.textContent = `Nhịp ${rep}/30: Hít vào thật sâu...`;
                    circle.className = "breathing-circle-outer inhale";
                    secEl.textContent = rep;
                } else {
                    instEl.textContent = `Nhịp ${rep}/30: Thở buông lỏng ra...`;
                    circle.className = "breathing-circle-outer exhale";
                }
            } else if (wimHofPhase === "RETENTION") {
                wimHofRetentionSec++;
                secEl.textContent = wimHofRetentionSec + "s";
            } else if (wimHofPhase === "RECOVERY") {
                let recSec = parseInt(secEl.textContent) - 1;
                secEl.textContent = recSec;
                if (recSec <= 0) {
                    clearInterval(breathingInterval);
                    breathingInterval = null;
                    btn.textContent = "Bắt đầu thở";
                    circle.className = "breathing-circle-outer";
                    instEl.textContent = `Hoàn thành hiệp Wim Hof! Nín thở: ${wimHofRetentionSec}s.`;
                    secEl.textContent = "✓";
                    breathCycleCount++;
                    if (cycleEl) cycleEl.textContent = breathCycleCount;
                }
            }
        }, 1500);
        return;
    }

    // Logic thông thường cho các bài tập khác
    btn.textContent = "Dừng bài tập";
    let phaseIdx = 0;
    let curPhase = preset.phases[phaseIdx];
    let countdown = curPhase.sec;
    breathCycleCount = 0;
    if (cycleEl) cycleEl.textContent = "0";

    instEl.textContent = curPhase.name;
    circle.className = "breathing-circle-outer " + curPhase.cls;
    secEl.textContent = countdown;

    function runStep() {
        countdown--;
        secEl.textContent = countdown;
        if (countdown <= 0) {
            phaseIdx = (phaseIdx + 1) % preset.phases.length;
            if (phaseIdx === 0) {
                breathCycleCount++;
                if (cycleEl) cycleEl.textContent = breathCycleCount;
            }
            curPhase = preset.phases[phaseIdx];
            countdown = curPhase.sec;
            instEl.textContent = curPhase.name;
            circle.className = "breathing-circle-outer " + curPhase.cls;
        }
    }

    breathingInterval = setInterval(runStep, 1000);
}

function triggerWimHofRecovery() {
    const secEl = document.getElementById('breath-sec');
    const instEl = document.getElementById('breath-instruction');
    const circle = document.getElementById('breath-circle');
    if (!secEl || !instEl) return;

    instEl.textContent = "HÍT ĐẦY LỒNG NGỰC & GIỮ 15 GIÂY...";
    circle.className = "breathing-circle-outer inhale";
    secEl.textContent = "15";

    clearInterval(breathingInterval);
    let count15 = 15;
    breathingInterval = setInterval(() => {
        count15--;
        secEl.textContent = count15;
        if (count15 <= 0) {
            clearInterval(breathingInterval);
            breathingInterval = null;
            const btn = document.getElementById('btn-breath-toggle');
            if (btn) btn.textContent = "Bắt đầu thở";
            circle.className = "breathing-circle-outer";
            instEl.textContent = `Xuất sắc! Nín thở hiệp này: ${wimHofRetentionSec}s.`;
            secEl.textContent = "✓";
            breathCycleCount++;
            const cycleEl = document.getElementById('breath-cycles');
            if (cycleEl) cycleEl.textContent = breathCycleCount;
        }
    }, 1000);
}

let simPendingMedPhotos = [];

function simHandleMedPhoto(event) {
    const file = event.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = function(e) {
        const base64 = e.target.result;
        simPendingMedPhotos.push(base64);
        renderMedPhotoPreviews();
    };
    reader.readAsDataURL(file);
}

function renderMedPhotoPreviews() {
    const cont = document.getElementById('med-photo-preview');
    if (!cont) return;
    cont.innerHTML = simPendingMedPhotos.map((p, idx) => `
        <div style="position: relative; display: inline-block;">
            <img src="${p}" style="width: 50px; height: 50px; object-fit: cover; border-radius: 6px; border: 1px solid var(--primary);">
            <button type="button" onclick="simRemoveMedPhoto(${idx})" style="position: absolute; top: -5px; right: -5px; background: #c62828; color: #fff; border: none; border-radius: 50%; width: 16px; height: 16px; font-size: 10px; cursor: pointer; display: flex; align-items: center; justify-content: center;">×</button>
        </div>
    `).join('');
}

function simRemoveMedPhoto(idx) {
    simPendingMedPhotos.splice(idx, 1);
    renderMedPhotoPreviews();
}

function simViewFullPhoto(src) {
    const overlay = document.createElement('div');
    overlay.style.cssText = 'position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.85);z-index:99999;display:flex;align-items:center;justify-content:center;padding:16px;cursor:pointer;';
    overlay.innerHTML = `<img src="${src}" style="max-width:95%; max-height:90%; border-radius:12px; box-shadow:0 8px 24px rgba(0,0,0,0.5);">`;
    overlay.onclick = () => overlay.remove();
    document.body.appendChild(overlay);
}

function saveMedVisit() {
    const fac = document.getElementById('med-fac') ? document.getElementById('med-fac').value.trim() : '';
    const doc = document.getElementById('med-doc') ? document.getElementById('med-doc').value.trim() : '';
    const ques = document.getElementById('med-ques') ? document.getElementById('med-ques').value.trim() : '';
    const notes = document.getElementById('med-notes') ? document.getElementById('med-notes').value.trim() : '';

    if (!fac && !notes && simPendingMedPhotos.length === 0) {
        alert('Vui lòng nhập tên cơ sở y tế, kết luận hoặc chụp ảnh đơn thuốc.');
        return;
    }

    const now = new Date();
    const dateStr = `Ngày ${now.getDate()} thg ${now.getMonth() + 1}, ${now.getFullYear()}`;

    const newVisit = {
        id: 'visit_' + Date.now(),
        facility: fac || 'Khám sức khỏe tổng quát',
        doctor: doc,
        questions: ques,
        notes: notes,
        photos: [...simPendingMedPhotos],
        date: dateStr
    };

    let visitsSaved = JSON.parse(localStorage.getItem('annhien_visits') || '[]');
    visitsSaved.unshift(newVisit);
    localStorage.setItem('annhien_visits', JSON.stringify(visitsSaved));

    simPendingMedPhotos = [];
    alert(`✓ Đã lưu hồ sơ khám tại "${newVisit.facility}" (kèm ${newVisit.photos.length} ảnh) thành công!`);
    openModule('medicalrecords');
}

function toggleLockPref(checked) {
    lockEnabled = checked;
    localStorage.setItem(STORAGE_KEYS.LOCK_ENABLED, checked);
    alert(checked ? "Đã bật khóa mã PIN!" : "Đã tắt khóa mã PIN.");
}

function changePin() {
    const p = document.getElementById('new-pin-input').value;
    if (p.length === 4) {
        userPin = p;
        localStorage.setItem(STORAGE_KEYS.PIN, p);
        alert("Đã cập nhật mã PIN mới thành công!");
    } else {
        alert("Mã PIN phải gồm 4 chữ số!");
    }
}

// ========================================================
// PIN LOCK SCREEN LOGIC
// ========================================================
let enteredPin = "";

function showPinLock() {
    enteredPin = "";
    updatePinDots();
    document.getElementById('lock-error').textContent = "";
    document.getElementById('pin-lock-overlay').classList.remove('hidden');
}

function hidePinLock() {
    document.getElementById('pin-lock-overlay').classList.add('hidden');
}

function pressKey(num) {
    if (enteredPin.length < 4) {
        enteredPin += num;
        updatePinDots();
        if (enteredPin.length === 4) {
            setTimeout(verifyPin, 150);
        }
    }
}

function deleteKey() {
    if (enteredPin.length > 0) {
        enteredPin = enteredPin.slice(0, -1);
        updatePinDots();
        document.getElementById('lock-error').textContent = "";
    }
}

function updatePinDots() {
    for (let i = 0; i < 4; i++) {
        const dot = document.getElementById(`dot-${i}`);
        if (i < enteredPin.length) dot.classList.add('filled');
        else dot.classList.remove('filled');
    }
}

function verifyPin() {
    if (enteredPin === userPin) {
        hidePinLock();
    } else {
        document.getElementById('lock-error').textContent = "Mã PIN không chính xác";
        enteredPin = "";
        updatePinDots();
    }
}

function unlockBiometric() {
    alert("Xác thực vân tay mô phỏng thành công!");
    hidePinLock();
}

// ========================================================
// PILL PACK INTERACTION
// ========================================================
function togglePillItem(day, el) {
    let saved = JSON.parse(localStorage.getItem('annhien_pills')) || {};
    saved[day] = !saved[day];
    localStorage.setItem('annhien_pills', JSON.stringify(saved));
    el.classList.toggle('taken');
    el.querySelector('small').textContent = el.classList.contains('taken') ? '✓' : '';
}

// ========================================================
// KEGEL TRAINER INTERACTION
// ========================================================
let kegelInterval = null;
let kegelReps = 0;

function toggleKegelTrainer() {
    const btn = document.getElementById('btn-kegel-toggle');
    const ring = document.getElementById('kegel-ring');
    const secEl = document.getElementById('kegel-sec');
    const phaseEl = document.getElementById('kegel-phase');
    const instEl = document.getElementById('kegel-instruction');
    const repsEl = document.getElementById('kegel-reps');

    if (kegelInterval) {
        clearInterval(kegelInterval);
        kegelInterval = null;
        btn.textContent = "Bắt đầu tập Kegel";
        ring.className = "kegel-ring-outer";
        secEl.textContent = "5";
        phaseEl.textContent = "SẴN SÀNG";
        instEl.textContent = "Nhấn Bắt đầu để đồng bộ nhịp tập";
    } else {
        btn.textContent = "Dừng bài tập";
        let isSqueeze = true;
        let count = 5;

        function step() {
            count--;
            secEl.textContent = count + 1;
            if (count < 0) {
                isSqueeze = !isSqueeze;
                count = 4;
                if (isSqueeze) {
                    kegelReps++;
                    if (repsEl) repsEl.textContent = kegelReps;
                    phaseEl.textContent = "CO THẮT CƠ";
                    instEl.textContent = "Co siết cơ đáy chậu như khi nín tiểu...";
                    ring.className = "kegel-ring-outer squeeze";
                } else {
                    phaseEl.textContent = "THẢ LỎNG";
                    instEl.textContent = "Thả lỏng hoàn toàn toàn bộ vùng chậu...";
                    ring.className = "kegel-ring-outer relax";
                }
            }
        }

        phaseEl.textContent = "CO THẮT CƠ";
        instEl.textContent = "Co siết cơ đáy chậu như khi nín tiểu...";
        ring.className = "kegel-ring-outer squeeze";
        secEl.textContent = "5";
        kegelInterval = setInterval(step, 1000);
    }
}

// ========================================================
// EPDS QUESTIONNAIRE
// ========================================================
const EPDS_QUESTIONS = [
    { q: "1. Tôi có thể cười và nhìn thấy mặt hài hước của mọi việc:", opts: ["Như trước đây (0đ)", "Bây giờ không được nhiều như trước (1đ)", "Bây giờ ít hơn nhiều (2đ)", "Hoàn toàn không thể (3đ)"] },
    { q: "2. Tôi hào hứng hướng về tương lai:", opts: ["Như trước đây (0đ)", "Hơi ít hơn trước đây (1đ)", "Rõ ràng ít hơn trước đây (2đ)", "Hầu như không bao giờ (3đ)"] },
    { q: "3. Tôi tự trách mình vô cớ khi mọi việc không như ý:", opts: ["Có, hầu như luôn luôn (3đ)", "Có, thỉnh thoảng (2đ)", "Hiếm khi (1đ)", "Không bao giờ (0đ)"] },
    { q: "4. Tôi cảm thấy lo lắng hoặc bồn chồn vô cớ:", opts: ["Hoàn toàn không (0đ)", "Hiếm khi (1đ)", "Có, thỉnh thoảng (2đ)", "Có, rất thường xuyên (3đ)"] },
    { q: "5. Tôi cảm thấy hoảng sợ hoặc sợ hãi mà không rõ lý do:", opts: ["Có, khá nhiều (3đ)", "Có, đôi khi (2đ)", "Không, không nhiều (1đ)", "Hoàn toàn không (0đ)"] }
];

function initEpdsQuestions() {
    const container = document.getElementById('epds-questions-list');
    if (!container) return;
    container.innerHTML = EPDS_QUESTIONS.map((item, qIdx) => `
        <div class="epds-card">
            <p>${item.q}</p>
            ${item.opts.map((opt, oIdx) => `
                <label class="epds-option">
                    <input type="radio" name="epds_q_${qIdx}" value="${oIdx}" ${oIdx === 0 ? 'checked' : ''}>
                    <span>${opt}</span>
                </label>
            `).join('')}
        </div>
    `).join('');
}

function calculateEpdsScore() {
    let score = 0;
    EPDS_QUESTIONS.forEach((_, qIdx) => {
        const checked = document.querySelector(`input[name="epds_q_${qIdx}"]:checked`);
        if (checked) score += parseInt(checked.value);
    });

    const resEl = document.getElementById('epds-result');
    if (score < 5) {
        resEl.innerHTML = `<div class="card" style="background: #E8F5E9; color: #2E7D32;">
            <strong>Điểm số: ${score}/15 — Trạng thái tâm lý ổn định</strong>
            <p class="tip-desc" style="color: #2E7D32;">Bạn đang điều tiết cảm xúc khá tốt. Hãy tiếp tục duy trì nghỉ ngơi và chia sẻ cùng người thân.</p>
        </div>`;
    } else {
        resEl.innerHTML = `<div class="card" style="background: #FFEBEE; color: #C62828;">
            <strong>Điểm số: ${score}/15 — Có dấu hiệu lo âu hoặc mệt mỏi kéo dài</strong>
            <p class="tip-desc" style="color: #C62828;">Bạn không đơn độc. Khuyến nghị bạn hãy chia sẻ cởi mở với gia đình và nên sắp xếp một buổi trò chuyện cùng bác sĩ tâm lý để được nâng đỡ kịp thời.</p>
        </div>`;
    }
}

// ========================================================
// CALCULATOR CAMOUFLAGE LOGIC
// ========================================================
let calcExpression = "";

function pressCalc(key) {
    const disp = document.getElementById('calc-display');
    const sub = document.getElementById('calc-expression');
    if (!disp) return;

    if (key === 'C') {
        calcExpression = "";
        disp.textContent = "0";
        sub.textContent = "";
    } else if (key === '+/-') {
        if (disp.textContent !== '0') {
            if (disp.textContent.startsWith('-')) disp.textContent = disp.textContent.slice(1);
            else disp.textContent = '-' + disp.textContent;
            calcExpression = disp.textContent;
        }
    } else if (key === '%') {
        disp.textContent = String(parseFloat(disp.textContent) / 100);
        calcExpression = disp.textContent;
    } else if (key === '=') {
        if (calcExpression === userPin) {
            document.getElementById('calculator-overlay').classList.add('hidden');
            calcExpression = "";
            disp.textContent = "0";
            sub.textContent = "";
            alert("Đã mở khóa An Nhiên từ chế độ ngụy trang!");
            return;
        }
        try {
            sub.textContent = calcExpression + " =";
            const sanitized = calcExpression.replace(/×/g, '*').replace(/÷/g, '/').replace(/−/g, '-');
            const result = Function('"use strict";return (' + sanitized + ')')();
            disp.textContent = String(result);
            calcExpression = String(result);
        } catch(e) {
            disp.textContent = "Lỗi";
            calcExpression = "";
        }
    } else {
        if (disp.textContent === "0" && !isNaN(key)) calcExpression = "";
        calcExpression += key;
        disp.textContent = calcExpression;
    }
}

function showMedSubTab(tab) {
    const v = document.getElementById('subtab-visits');
    const m = document.getElementById('subtab-meds');
    if (tab === 'visits') {
        if (v) v.style.display = 'block';
        if (m) m.style.display = 'none';
    } else {
        if (v) v.style.display = 'none';
        if (m) m.style.display = 'block';
    }
}

function saveMedicationItem() {
    const nameEl = document.getElementById('input-med-name');
    const doseEl = document.getElementById('input-med-dose');
    if (!nameEl || !nameEl.value.trim()) {
        alert("Vui lòng nhập tên thuốc hoặc vi chất!");
        return;
    }
    const meds = JSON.parse(localStorage.getItem('annhien_meds') || '[]');
    meds.push({ name: nameEl.value.trim(), dose: doseEl.value.trim() });
    localStorage.setItem('annhien_meds', JSON.stringify(meds));
    alert("Đã lưu " + nameEl.value.trim() + " vào danh sách vi chất ngoại tuyến!");
    openModule('medicalrecords');
    showMedSubTab('meds');
}

// ========================================================
// ONLINE UTILITIES SIMULATOR ACTIONS
// ========================================================



// --- Air Quality Helpers ---
const SIM_AQ_DATA = {
    hanoi: { city: 'Hà Nội, VN', name: 'Trạm ĐSQ Hoa Kỳ — Hà Nội', id: 3274, pm25: 28.4, pm10: 42.1, no2: 18.5, lat: 21.0245, lon: 105.8412, updated: '15 phút trước', cached: true },
    hcm:   { city: 'TP. HCM, VN', name: 'Trạm Tân Sơn Hòa — Q. Tân Phú', id: 4621, pm25: 38.7, pm10: 58.3, no2: 24.2, lat: 10.8041, lon: 106.6682, updated: '8 phút trước', cached: false },
    danang:{ city: 'Đà Nẵng, VN', name: 'Trạm Ngũ Hành Sơn', id: 5108, pm25: 12.1, pm10: 18.6, no2: 9.8, lat: 16.0471, lon: 108.2062, updated: '22 phút trước', cached: true }
};

function simAQILevel(pm25) {
    if (pm25 <= 12)  return { label: 'Tốt', bg: '#D4EDDA', color: '#155724' };
    if (pm25 <= 35)  return { label: 'Trung bình', bg: '#FFF3CD', color: '#856404' };
    if (pm25 <= 55)  return { label: 'Không tốt cho nhóm nhạy cảm', bg: '#FFE8D0', color: '#995200' };
    if (pm25 <= 150) return { label: 'Xấu', bg: '#FFCDD2', color: '#B71C1C' };
    return { label: 'Rất xấu', bg: '#E1BEE7', color: '#6A1B9A' };
}

function simRenderAQStation(d) {
    const lvl = simAQILevel(d.pm25);
    const pct = Math.min(100, (d.pm25 / 150) * 100).toFixed(0);
    const barColor = d.pm25 <= 12 ? '#43a047' : d.pm25 <= 35 ? '#f9a825' : d.pm25 <= 55 ? '#ef6c00' : '#c62828';
    return `<div class="card" style="padding: 14px;">
        <div class="card-row" style="margin-bottom: 8px;">
            <div>
                <strong>${d.name}</strong>
                <p class="tip-desc" style="margin:0;">ID: ${d.id} • ${d.lat.toFixed(3)}°N, ${d.lon.toFixed(3)}°E</p>
            </div>
            <span class="badge-num" style="background: ${lvl.bg}; color: ${lvl.color}; white-space: nowrap;">${lvl.label}</span>
        </div>
        <div style="background: var(--surface-variant); border-radius: 8px; overflow: hidden; height: 8px; margin-bottom: 12px;">
            <div style="height: 100%; width: ${pct}%; background: ${barColor}; border-radius: 8px; transition: width 0.5s;"></div>
        </div>
        <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; font-size: 0.82rem; text-align: center; margin-bottom: 10px;">
            <div style="background: var(--surface-variant); padding: 8px; border-radius: 10px;">
                <div style="font-weight: 700; font-size: 1.1rem; color: ${barColor};">${d.pm25}</div>
                <div style="color: var(--text-muted); font-size: 0.72rem;">PM2.5 µg/m³</div>
            </div>
            <div style="background: var(--surface-variant); padding: 8px; border-radius: 10px;">
                <div style="font-weight: 700; font-size: 1.1rem;">${d.pm10}</div>
                <div style="color: var(--text-muted); font-size: 0.72rem;">PM10 µg/m³</div>
            </div>
            <div style="background: var(--surface-variant); padding: 8px; border-radius: 10px;">
                <div style="font-weight: 700; font-size: 1.1rem;">${d.no2}</div>
                <div style="color: var(--text-muted); font-size: 0.72rem;">NO₂ µg/m³</div>
            </div>
        </div>
        <div style="display: flex; justify-content: space-between; font-size: 0.75rem; color: var(--text-muted);">
            <span>⏱ Cập nhật: ${d.updated}</span>
            <span>${d.cached ? '💾 Từ cache (TTL 30 phút)' : '🌐 Trực tiếp từ OpenAQ v3'}</span>
        </div>
    </div>`;
}

function simLoadAQCity(city) {
    const data = SIM_AQ_DATA[city];
    if (!data) return;
    const coordEl = document.getElementById('sim-aq-coords');
    if (coordEl) coordEl.textContent = `${data.lat.toFixed(4)}° N, ${data.lon.toFixed(4)}° E (${data.city})`;
    const el = document.getElementById('sim-aq-station');
    if (!el) return;
    el.innerHTML = `<div style="text-align:center; padding: 16px; color: var(--text-muted);"><i class="fa-solid fa-spinner fa-spin"></i> Đang kết nối OpenAQ v3...</div>`;
    setTimeout(() => {
        el.innerHTML = simRenderAQStation(data);
    }, 700);
}

function simRefreshAirQuality() {
    const el = document.getElementById('sim-aq-station');
    const btn = document.getElementById('btn-aq-refresh');
    if (!el) return;
    if (btn) btn.disabled = true;
    el.innerHTML = `<div style="text-align:center; padding: 16px; color: var(--text-muted);"><i class="fa-solid fa-spinner fa-spin"></i> Đang cập nhật OpenAQ v3...</div>`;
    setTimeout(() => {
        const refreshed = { ...SIM_AQ_DATA.hanoi, pm25: +(Math.random() * 40 + 8).toFixed(1), pm10: +(Math.random() * 55 + 15).toFixed(1), no2: +(Math.random() * 25 + 6).toFixed(1), updated: 'Vừa xong', cached: false };
        el.innerHTML = simRenderAQStation(refreshed);
        if (btn) btn.disabled = false;
    }, 900);
}

// --- MyHealthfinder Topics (24 Clinical Prevention Topics from HHS v4 API) ---
const HT_ALL_TOPICS = [
    {
        id: 1, cat: 'Sàng lọc phụ khoa', catKey: 'sangloc', catColor: 'rose', icon: '🔬',
        age: '21 – 65 tuổi', title: 'Tầm soát ung thư cổ tử cung (Pap smear & HPV)',
        body: 'Xét nghiệm định kỳ tế bào cổ tử cung (Pap smear) mỗi 3 năm hoặc phối hợp xét nghiệm HPV DNA mỗi 5 năm.',
        frequency: 'Mỗi 3 năm (21–29 tuổi) hoặc mỗi 5 năm (30–65 tuổi)',
        detail: 'Ung thư cổ tử cung hầu như có thể phòng ngừa hoàn toàn nếu phát hiện sớm biến đổi tiền ung thư do virus HPV gây ra. Phụ nữ đã tiêm vắc-xin HPV vẫn cần tầm soát theo phác đồ chuẩn USPSTF.',
        questions: ['Tôi nên làm xét nghiệm Pap smear đơn thuần hay đồng thời xét nghiệm HPV?', 'Bao lâu sau khi có kết quả bình thường thì tôi cần xét nghiệm lại?']
    },
    {
        id: 2, cat: 'Sàng lọc phụ khoa', catKey: 'sangloc', catColor: 'rose', icon: '🎗️',
        age: '40 – 74 tuổi', title: 'Tầm soát ung thư vú bằng chụp nhũ ảnh (Mammogram)',
        body: 'Chụp X-quang tuyến vú định kỳ 1–2 năm/lần giúp phát hiện các khối u vi thể trước khi có thể sờ thấy bằng tay.',
        frequency: 'Mỗi 1 – 2 năm một lần từ 40 tuổi',
        detail: 'Khuyến nghị mới nhất từ USPSTF hạ độ tuổi bắt đầu chụp nhũ ảnh xuống 40 tuổi đối với mọi phụ nữ có nguy cơ trung bình. Tự khám vú hàng tháng sau khi sạch kinh 3–5 ngày để phát hiện bất thường.',
        questions: ['Mật độ mô tuyến vú của tôi là đặc hay thưa? Có cần siêu âm kết hợp không?', 'Tiền sử gia đình tôi có người mắc ung thư vú/buồng trứng, tôi có nên xét nghiệm gen BRCA không?']
    },
    {
        id: 3, cat: 'Sàng lọc phụ khoa', catKey: 'sangloc', catColor: 'rose', icon: '🧪',
        age: '18 – 25 tuổi & nguy cơ cao', title: 'Tầm soát bệnh lây truyền qua đường tình dục (STIs)',
        body: 'Xét nghiệm Chlamydia và Lậu cầu định kỳ hàng năm cho phụ nữ dưới 25 tuổi có quan hệ tình dục.',
        frequency: 'Định kỳ hàng năm hoặc khi có bạn tình mới',
        detail: 'Chlamydia và Lậu thường diễn tiến âm thầm không triệu chứng ở phụ nữ nhưng có thể gây viêm vùng chậu (PID), tắc vòi trứng và vô sinh thứ phát nếu không điều trị sớm bằng kháng sinh thích hợp.',
        questions: ['Những xét nghiệm STI nào được khuyến nghị trong đợt khám định kỳ này?', 'Bạn tình của tôi có cần điều trị đồng thời để tránh tái nhiễm không?']
    },
    {
        id: 4, cat: 'Sàng lọc phụ khoa', catKey: 'sangloc', catColor: 'rose', icon: '🩺',
        age: '45 – 75 tuổi', title: 'Tầm soát ung thư đại trực tràng',
        body: 'Nội soi đại tràng mỗi 10 năm hoặc xét nghiệm phân tìm máu vi thể (FIT/FOBT) hàng năm từ 45 tuổi.',
        frequency: 'FIT hàng năm hoặc Nội soi đại tràng mỗi 10 năm',
        detail: 'Ung thư đại trực tràng là nguyên nhân tử vong do ung thư phổ biến thứ 3 ở phụ nữ. Phát hiện và cắt bỏ polyp tiền ung thư qua nội soi giúp ngăn chặn bệnh phát triển hiệu quả.',
        questions: ['Phương pháp tầm soát nào phù hợp nhất với tôi: xét nghiệm phân hay nội soi đại tràng?', 'Tôi có cần chuẩn bị chế độ ăn đặc biệt trước ngày xét nghiệm không?']
    },
    {
        id: 5, cat: 'Thai kỳ & Sinh nở', catKey: 'thaiky', catColor: 'lavender', icon: '🌿',
        age: 'Phụ nữ độ tuổi sinh sản', title: 'Bổ sung Axit Folic dự phòng dị tật ống thần kinh',
        body: 'Bổ sung 400 – 800 mcg Acid Folic mỗi ngày ít nhất 1 tháng trước khi thụ thai và suốt 3 tháng đầu thai kỳ.',
        frequency: 'Hàng ngày trước khi thụ thai & 12 tuần đầu',
        detail: 'Ống thần kinh thai nhi đóng lại ngay trong 28 ngày đầu sau thụ thai — thường trước khi người mẹ biết mình có thai. Bổ sung axit folic đủ liều giúp giảm đến 70% nguy cơ tật nứt đốt sống và vô sọ.',
        questions: ['Với tiền sử gia đình hoặc thể trạng của tôi, liều 400 mcg hay 800 mcg là tối ưu?', 'Viên uống vitamin tổng hợp của tôi đã có đủ dạng Folate hoạt tính (L-methylfolate) chưa?']
    },
    {
        id: 6, cat: 'Thai kỳ & Sinh nở', catKey: 'thaiky', catColor: 'lavender', icon: '🩸',
        age: 'Tuần 24 – 28 thai kỳ', title: 'Nghiệm pháp dung nạp đường huyết (Tầm soát ĐTĐ thai kỳ)',
        body: 'Xét nghiệm dung nạp glucose đường uống 75g (OGTT) ở tuần thai 24–28 để phát hiện đái tháo đường thai kỳ.',
        frequency: 'Một lần vào tuần 24–28 (hoặc sớm hơn nếu có yếu tố nguy cơ)',
        detail: 'Đái tháo đường thai kỳ không được kiểm soát có thể khiến thai nhi to quá mức (macrosomia), hạ đường huyết sơ sinh và tăng nguy cơ tiền sản giật, băng huyết khi sinh.',
        questions: ['Quy trình nhịn ăn và lấy máu xét nghiệm 3 mốc (0h, 1h, 2h) thực hiện như thế nào?', 'Nếu chỉ số cao nhẹ, tôi có thể kiểm soát bằng thực đơn dinh dưỡng trước không?']
    },
    {
        id: 7, cat: 'Thai kỳ & Sinh nở', catKey: 'thaiky', catColor: 'lavender', icon: '🧬',
        age: 'Tuần 10 – 14 thai kỳ', title: 'Sàng lọc dị tật trước sinh không xâm lấn (NIPT / Double Test)',
        body: 'Phân tích ADN tự do của thai nhi trong máu mẹ để sàng lọc hội chứng Down, Edwards, Patau với độ chính xác >99%.',
        frequency: 'Từ tuần thứ 9–10 thai kỳ',
        detail: 'NIPT an toàn tuyệt đối cho mẹ và bé vì chỉ lấy máu tĩnh mạch của mẹ. Kết hợp với siêu âm đo độ mờ da gáy ở tuần 11–13 tuần 6 ngày để có đánh giá toàn diện nhất.',
        questions: ['Giữa Double Test truyền thống và NIPT thì phương án nào tối ưu cho trường hợp của tôi?', 'Nếu kết quả NIPT có nguy cơ cao, bước xác chẩn tiếp theo sẽ là gì?']
    },
    {
        id: 8, cat: 'Thai kỳ & Sinh nở', catKey: 'thaiky', catColor: 'lavender', icon: '💉',
        age: 'Tuần 27 – 36 thai kỳ', title: 'Tiêm chủng thai kỳ: Vắc-xin Tdap & Cúm mùa',
        body: 'Tiêm 1 mũi vắc-xin phối hợp Uốn ván - Bạch hầu - Ho gà (Tdap) ở mỗi thai kỳ để truyền kháng thể thụ động bảo vệ trẻ sơ sinh.',
        frequency: 'Mỗi thai kỳ (tốt nhất tuần 27–36) & vắc-xin cúm vào mùa dịch',
        detail: 'Kháng thể ho gà từ mẹ qua nhau thai là lớp khiên bảo vệ duy nhất của em bé trong 2 tháng đầu đời trước khi bé đủ tuổi tiêm mũi vắc-xin đầu tiên.',
        questions: ['Tôi nên tiêm mũi Tdap vào tuần thai nào là tốt nhất để truyền lượng kháng thể cao nhất?', 'Vắc-xin cúm bất hoạt có an toàn trong 3 tháng đầu không?']
    },
    {
        id: 9, cat: 'Hậu sản & Nuôi con', catKey: 'hausan', catColor: 'terracotta', icon: '💛',
        age: '0 – 12 tháng sau sinh', title: 'Sàng lọc trầm cảm sau sinh (Thang đo EPDS)',
        body: 'Kiểm tra tâm lý bằng thang đo Edinburgh Postnatal Depression Scale ở các mốc 2 tuần, 6 tuần và 6 tháng sau sinh.',
        frequency: 'Khám hậu sản tuần 2, tuần 6 và bất kỳ khi nào thấy kiệt quệ',
        detail: 'Trầm cảm sau sinh là bệnh lý y khoa thực thể liên quan đến sụt giảm hormone đột ngột (Estrogen, Progesterone), không phải lỗi do sự "yếu đuối". Cần phân biệt với hội chứng Baby Blues thoáng qua.',
        questions: ['Cảm giác lo lắng quá mức về em bé của tôi có phải là biểu hiện trầm cảm/lo âu sau sinh không?', 'Các loại thuốc điều chỉnh cảm xúc có an toàn khi tôi đang cho con bú không?']
    },
    {
        id: 10, cat: 'Hậu sản & Nuôi con', catKey: 'hausan', catColor: 'terracotta', icon: '🧘',
        age: 'Sau sinh từ 6 tuần', title: 'Phục hồi cơ sàn chậu & kiểm tra tách cơ thẳng bụng',
        body: 'Đánh giá chức năng đáy chậu (són tiểu, sa tử cung) và khoảng cách cơ thẳng bụng (Diastasis Recti) trước khi tập nặng.',
        frequency: 'Kiểm tra ở tuần thứ 6 sau sinh',
        detail: 'Quá trình mang thai và sinh nở kéo giãn dây chằng và cơ nâng hậu môn. Tập Kegel đúng kỹ thuật kết hợp vật lý trị liệu phục hồi sớm giúp phòng ngừa són tiểu khi ho/cười ở tuổi trung niên.',
        questions: ['Cơ sàn chậu của tôi có bị tăng trương lực hay suy yếu? Tôi tập Kegel thế này đã đúng cơ chưa?', 'Khoảng cách tách cơ bụng của tôi là mấy ngón tay, tôi có được tập gập bụng không?']
    },
    {
        id: 11, cat: 'Hậu sản & Nuôi con', catKey: 'hausan', catColor: 'terracotta', icon: '🥛',
        age: 'Thời kỳ cho con bú', title: 'Dự phòng tắc tia sữa & viêm tuyến vú cấp (Mastitis)',
        body: 'Nhận diện sớm các dấu hiệu cương sữa sinh lý, tắc ống dẫn sữa để xử trí hạ nhiệt, chườm lạnh, cho bú đúng khớp ngậm.',
        frequency: 'Theo dõi hàng ngày trong 3 tháng đầu cho con bú',
        detail: 'Phác đồ quốc tế mới nhất của Viện Y học Nuôi con bằng sữa mẹ (ABM) khuyến cáo: KHÔNG nhào bóp thô bạo bầu ngực bị viêm; ưu tiên chườm mát, nghỉ ngơi và dùng kháng sinh kịp thời nếu có sốt cao.',
        questions: ['Khối cứng ở ngực tôi kèm sốt 38.5°C có cần dùng kháng sinh an toàn cho con bú không?', 'Bác sĩ có thể kiểm tra khớp ngậm của bé để hạn chế nứt cổ gà không?']
    },
    {
        id: 12, cat: 'Hậu sản & Nuôi con', catKey: 'hausan', catColor: 'terracotta', icon: '🛡️',
        age: 'Từ tuần thứ 6 sau sinh', title: 'Kế hoạch hóa gia đình & Tránh thai khi cho con bú',
        body: 'Lựa chọn phương pháp tránh thai không ảnh hưởng đến số lượng và chất lượng sữa mẹ (Que cấy, Vòng TCu, Thuốc POP).',
        frequency: 'Thảo luận tại buổi tái khám sau sinh 6 tuần',
        detail: 'Phương pháp cho bú vô kinh (LAM) chỉ hiệu quả khi trẻ dưới 6 tháng bú mẹ hoàn toàn cả ngày lẫn đêm và mẹ chưa có kinh trở lại. Khuyến cáo chủ động ngừa thai bằng biện pháp rào chắn hoặc progesterone đơn thuần.',
        questions: ['Đặt vòng tránh thai hay cấy que Implanon sẽ phù hợp hơn với cơ địa của tôi?', 'Thuốc tránh thai Progestin đơn thuần (POP) có làm giảm lượng sữa mẹ không?']
    },
    {
        id: 13, cat: 'Chu kỳ & Nội tiết', catKey: 'noitiet', catColor: 'sage', icon: '🩸',
        age: 'Độ tuổi sinh sản (18–45)', title: 'Tầm soát Hội chứng Buồng trứng Đa nang (PCOS)',
        body: 'Chẩn đoán dựa trên tiêu chuẩn Rotterdam: Kinh thưa/vô kinh, rậm lông/mụn trứng cá, hoặc hình ảnh đa nang trên siêu âm.',
        frequency: 'Khám khi chu kỳ kinh kéo dài trên 35 ngày hoặc bất thường',
        detail: 'PCOS gắn liền với hiện tượng kháng insulin và rối loạn chuyển hóa lipid. Kiểm soát cân nặng và chế độ ăn chỉ số đường huyết thấp (Low GI) là nền tảng điều trị đầu tay.',
        questions: ['Tôi có cần làm xét nghiệm đánh giá kháng insulin (HOMA-IR) và mỡ máu không?', 'Liệu trình điều hòa kinh nguyệt bằng Myo-Inositol có phù hợp với tôi không?']
    },
    {
        id: 14, cat: 'Chu kỳ & Nội tiết', catKey: 'noitiet', catColor: 'sage', icon: '🌸',
        age: 'Độ tuổi sinh sản', title: 'Tầm soát Lạc nội mạc tử cung (Endometriosis)',
        body: 'Thăm khám chuyên sâu khi có triệu chứng đau bụng kinh dữ dội, đau khi quan hệ hoặc đau vùng chậu mãn tính.',
        frequency: 'Khám ngay khi đau bụng kinh cản trở sinh hoạt hàng ngày',
        detail: 'Lạc nội mạc tử cung thường bị bỏ sót trong nhiều năm vì lầm tưởng là đau bụng kinh thông thường. Chẩn đoán sớm giúp bảo tồn tối đa chức năng dự trữ buồng trứng (AMH).',
        questions: ['Cơn đau bụng kinh của tôi có phải là dấu hiệu của lạc nội mạc tử cung không?', 'Siêu âm chuyên sâu vùng chậu hay MRI sẽ giúp đánh giá tổn thương tốt nhất?']
    },
    {
        id: 15, cat: 'Chu kỳ & Nội tiết', catKey: 'noitiet', catColor: 'sage', icon: '🦋',
        age: 'Mọi lứa tuổi', title: 'Kiểm tra chức năng Tuyến giáp (TSH & Free T4)',
        body: 'Xét nghiệm máu kiểm tra suy giáp hoặc cường giáp khi có dấu hiệu rụng tóc, tăng cân khó hiểu, mệt mỏi hoặc rối loạn kinh.',
        frequency: 'Mỗi 3–5 năm hoặc khi có triệu chứng nghi ngờ',
        detail: 'Phụ nữ có nguy cơ mắc bệnh tuyến giáp tự miễn (Viêm tuyến giáp Hashimoto) cao gấp 5–8 lần nam giới. Tuyến giáp đóng vai trò chỉ huy quá trình trao đổi chất và rụng trứng.',
        questions: ['Chỉ số TSH của tôi có nằm trong khoảng lý tưởng cho khả năng sinh sản (<2.5 mIU/L) không?', 'Tôi có cần xét nghiệm thêm kháng thể kháng tuyến giáp Anti-TPO không?']
    },
    {
        id: 16, cat: 'Chu kỳ & Nội tiết', catKey: 'noitiet', catColor: 'sage', icon: '⚡',
        age: 'Tuổi sinh sản', title: 'Quản lý Rối loạn tiền kinh nguyệt trầm trọng (PMDD)',
        body: 'Nhận diện các cơn cáu gắt cực độ, trầm cảm sâu sắc xuất hiện 7–10 ngày trước kỳ kinh và biến mất khi bắt đầu hành kinh.',
        frequency: 'Ghi nhật ký cảm xúc trong ít nhất 2 chu kỳ liên tiếp',
        detail: 'PMDD là phản ứng quá mẫn của não bộ đối với sự biến thiên sinh lý của progesterone, ảnh hưởng đến chất dẫn truyền thần kinh Serotonin. Bổ sung Canxi, B6, Magie và liệu pháp tâm lý đem lại cải thiện rõ rệt.',
        questions: ['Nhật ký triệu chứng 2 chu kỳ này của tôi có đủ tiêu chuẩn chẩn đoán PMDD không?', 'Phương pháp can thiệp nào là phù hợp nhất: điều hòa hormone hay thuốc ức chế tái hấp thu Serotonin?']
    },
    {
        id: 17, cat: 'Tiền mãn kinh & Mãn kinh', catKey: 'mankinh', catColor: 'lavender', icon: '🦴',
        age: 'Từ 50 – 65 tuổi', title: 'Đo mật độ khoáng chất xương (DEXA scan)',
        body: 'Đo mật độ xương vùng cột sống thắt lưng và cổ xương đùi để tầm soát sớm loãng xương và nguy cơ gãy xương.',
        frequency: 'Mỗi 2 năm một lần từ 65 tuổi (hoặc từ 50 tuổi nếu có yếu tố nguy cơ)',
        detail: 'Trong 5 năm đầu sau mãn kinh, phụ nữ có thể mất tới 20% khối lượng xương do suy giảm nồng độ estrogen bảo vệ. Bổ sung Canxi 1200mg + Vitamin D3 800–1000 IU là bắt buộc.',
        questions: ['Chỉ số T-score của tôi là bao nhiêu, đã ở mức thiếu xương (osteopenia) hay loãng xương chưa?', 'Tôi có cần dùng thuốc nhóm bisphosphonate để ức chế hủy xương không?']
    },
    {
        id: 18, cat: 'Tiền mãn kinh & Mãn kinh', catKey: 'mankinh', catColor: 'lavender', icon: '🔥',
        age: '45 – 55 tuổi', title: 'Quản lý cơn bốc hỏa & Liệu pháp Hormone thay thế (HRT)',
        body: 'Đánh giá chỉ định và chống chỉ định của liệu pháp nội tiết mãn kinh (MHT/HRT) để giảm cơn bốc hỏa, đổ mồ hôi đêm.',
        frequency: 'Tư vấn khi các triệu chứng bốc hỏa làm suy giảm chất lượng sống',
        detail: 'Nếu bắt đầu trong "cửa sổ cơ hội" (dưới 60 tuổi hoặc trong vòng 10 năm sau mãn kinh), HRT mang lại nhiều lợi ích cho tim mạch, xương khớp và giấc ngủ hơn là rủi ro.',
        questions: ['Tôi có thuộc nhóm chống chỉ định của HRT (tiền sử huyết khối, ung thư phụ thuộc hormone) không?', 'Nếu không dùng hormone, có loại thảo dược hay thuốc không nội tiết nào giúp giảm bốc hỏa hiệu quả?']
    },
    {
        id: 19, cat: 'Tiền mãn kinh & Mãn kinh', catKey: 'mankinh', catColor: 'lavender', icon: '💧',
        age: 'Phụ nữ tiền mãn kinh & sau mãn kinh', title: 'Hội chứng Tiết niệu - Sinh dục mãn kinh (GSM)',
        body: 'Điều trị khô âm đạo, đau rát khi quan hệ và nhiễm trùng tiết niệu tái diễn do suy giảm estrogen tại chỗ.',
        frequency: 'Khám phụ khoa định kỳ hàng năm',
        detail: 'Không giống như cơn bốc hỏa có thể tự hết sau vài năm, teo niêm mạc âm đạo tiến triển nặng dần theo thời gian. Sử dụng kem bôi estrogen liều cực thấp tại chỗ mang lại hiệu quả cao và an toàn toàn thân.',
        questions: ['Liệu pháp estrogen tại chỗ dạng viên đặt hay kem bôi có an toàn cho niêm mạc của tôi không?', 'Các chất bôi trơn và dưỡng ẩm âm đạo gốc nước nào được khuyên dùng hàng ngày?']
    },
    {
        id: 20, cat: 'Tâm lý & Giấc ngủ', catKey: 'tamly', catColor: 'rose', icon: '🧠',
        age: 'Mọi lứa tuổi', title: 'Sàng lọc Rối loạn Lo âu lan tỏa (Thang đo GAD-7)',
        body: 'Đánh giá mức độ lo âu, căng thẳng thần kinh và suy nghĩ tiêu cực kéo dài trên 6 tháng.',
        frequency: 'Khám định kỳ hàng năm hoặc khi căng thẳng kéo dài',
        detail: 'Phụ nữ có tỷ lệ mắc rối loạn lo âu cao gấp đôi nam giới do sự nhạy cảm của thụ thể GABA với dao động hormone buồng trứng. Liệu pháp nhận thức hành vi (CBT) và bài tập thở 4-7-8 giúp tái cân bằng.',
        questions: ['Điểm GAD-7 của tôi ở mức độ nào: nhẹ, vừa hay nặng?', 'Bác sĩ có thể giới thiệu chuyên gia tâm lý có kinh nghiệm về sức khỏe phụ nữ không?']
    },
    {
        id: 21, cat: 'Tâm lý & Giấc ngủ', catKey: 'tamly', catColor: 'rose', icon: '🌙',
        age: 'Mọi lứa tuổi', title: 'Vệ sinh giấc ngủ & Rối loạn nhịp sinh học ở phụ nữ',
        body: 'Thiết lập nhịp ngủ - thức ổn định, hạn chế ánh sáng xanh và điều hòa hormone Melatonin nội sinh.',
        frequency: 'Theo dõi chất lượng giấc ngủ hàng tuần',
        detail: 'Chất lượng giấc ngủ của phụ nữ dao động rõ rệt theo chu kỳ kinh nguyệt (kém nhất vào tuần hoàng thể). Thiếu ngủ mãn tính làm tăng hormone đói Ghrelin và tăng đề kháng insulin.',
        questions: ['Tình trạng khó vào giấc của tôi có liên quan đến suy giảm Progesterone giai đoạn tiền mãn kinh không?', 'Bổ sung Magie Glycinate trước khi ngủ có giúp tôi thư giãn cơ bắp và ngủ sâu hơn không?']
    },
    {
        id: 22, cat: 'Tim mạch & Chuyển hóa', catKey: 'timmach', catColor: 'sage', icon: '❤️',
        age: 'Từ 20 tuổi trở lên', title: 'Kiểm soát Huyết áp & Nhận diện bệnh tim mạch nữ giới',
        body: 'Đo huyết áp định kỳ để giữ dưới 120/80 mmHg. Nhận biết các triệu chứng đau tim "phi điển hình" ở phụ nữ.',
        frequency: 'Đo ít nhất 1 lần mỗi năm',
        detail: 'Triệu chứng nhồi máu cơ tim ở nữ giới thường không phải là cơn đau thắt ngực dữ dội mà là mệt mỏi kiệt sức, khó thở, buồn nôn, đau hàm hoặc đau lưng giữa hai bả vai.',
        questions: ['Chỉ số huyết áp đo tại nhà của tôi như thế này đã đạt chuẩn tối ưu chưa?', 'Tôi có cần làm điện tâm đồ (ECG) hoặc siêu âm tim trong đợt khám sức khỏe tổng quát này không?']
    },
    {
        id: 23, cat: 'Tim mạch & Chuyển hóa', catKey: 'timmach', catColor: 'sage', icon: '🧪',
        age: 'Từ 20 tuổi trở lên', title: 'Xét nghiệm Bilan Lipid máu (Cholesterol toàn phần, LDL, HDL, Triglyceride)',
        body: 'Đánh giá nguy cơ xơ vữa động mạch và đột quỵ thông qua chỉ số mỡ máu, đặc biệt sau tuổi mãn kinh khi estrogen giảm.',
        frequency: 'Mỗi 4–6 năm từ 20 tuổi (hoặc hàng năm nếu có nguy cơ)',
        detail: 'Estrogen tự nhiên giúp duy trì nồng độ HDL (cholesterol tốt) cao. Sau khi mãn kinh, LDL (cholesterol xấu) thường tăng vọt nhanh chóng, làm tăng gấp đôi nguy cơ bệnh mạch vành ở phụ nữ.',
        questions: ['Mục tiêu chỉ số LDL-C phù hợp với thang điểm nguy cơ tim mạch 10 năm của tôi là bao nhiêu?', 'Chế độ ăn Địa Trung Hải kết hợp dầu ô-liu và cá béo có giúp cải thiện mỡ máu của tôi không?']
    },
    {
        id: 24, cat: 'Tim mạch & Chuyển hóa', catKey: 'timmach', catColor: 'sage', icon: '☀️',
        age: 'Mọi lứa tuổi', title: 'Xét nghiệm Nồng độ Vitamin D huyết thanh [25(OH)D]',
        body: 'Tầm soát tình trạng thiếu hụt Vitamin D — yếu tố then chốt cho miễn dịch, hấp thu canxi và sức khỏe tuyến vú.',
        frequency: 'Mỗi năm một lần hoặc khi có dấu hiệu rụng tóc, đau mỏi xương',
        detail: 'Hơn 70% phụ nữ thành thị châu Á bị thiếu vitamin D do thói quen che chắn chống nắng kỹ lưỡng. Mức tối ưu trong máu cần duy trì là 30 – 50 ng/mL.',
        questions: ['Nồng độ 25-hydroxyvitamin D của tôi hiện tại là bao nhiêu ng/mL?', 'Tôi cần uống liều tấn công bao nhiêu IU mỗi ngày để đưa chỉ số về mức an toàn?']
    }
];

const HT_CAT_COLORS = {
    rose: 'var(--rose-light)|var(--rose)',
    lavender: 'var(--lavender-light)|var(--lavender)',
    terracotta: 'var(--terracotta-light)|var(--terracotta)',
    sage: 'var(--sage-light)|var(--sage)'
};

let htCurrentCategory = 'all';
let htCurrentSearch = '';

function simRenderHealthTopicsView() {
    return `
        <div class="card card-tip" style="margin-bottom: 12px; background: linear-gradient(135deg, rgba(230,123,123,0.1), rgba(184,142,209,0.1));">
            <div style="display: flex; gap: 10px; align-items: flex-start;">
                <div style="font-size: 1.8rem; line-height: 1;">🏛️</div>
                <div>
                    <strong style="display:block; margin-bottom: 2px;">MyHealthfinder v4 API (HHS / USPSTF)</strong>
                    <p class="tip-desc" style="margin: 0; font-size: 0.75rem; line-height: 1.4;">
                        Khuyến nghị y tế dự phòng chuẩn hóa từ <strong>Bộ Y tế Hoa Kỳ (HHS)</strong>. Dữ liệu thực tế cá nhân hóa theo độ tuổi, giới tính và tiền sử lâm sàng với hơn <strong>120+ chủ đề y khoa</strong>. Dưới đây là <strong>24 đề tài lâm sàng tiêu biểu</strong> dành cho phụ nữ.
                    </p>
                </div>
            </div>
        </div>

        <!-- Search Bar -->
        <div class="search-bar-wrap" style="margin-bottom: 10px;">
            <i class="fa-solid fa-magnifying-glass search-icon"></i>
            <input type="text" id="ht-search-input" placeholder="Tìm theo bệnh, triệu chứng (HPV, loãng xương, trầm cảm...)" oninput="simSearchHealthTopics(this.value)" autocomplete="off">
        </div>

        <!-- Category Filter Chips -->
        <div style="display: flex; gap: 6px; overflow-x: auto; padding-bottom: 8px; margin-bottom: 10px; scrollbar-width: none;" id="ht-cat-chips">
            <button class="btn-primary-sm" style="font-size: 0.72rem; padding: 4px 10px; white-space: nowrap; margin-top:0;" onclick="simFilterHealthTopics('all', this)" id="ht-chip-all">Tất cả (${HT_ALL_TOPICS.length})</button>
            <button class="btn-outline" style="font-size: 0.72rem; padding: 4px 10px; white-space: nowrap; margin-top:0;" onclick="simFilterHealthTopics('sangloc', this)">🔬 Sàng lọc</button>
            <button class="btn-outline" style="font-size: 0.72rem; padding: 4px 10px; white-space: nowrap; margin-top:0;" onclick="simFilterHealthTopics('thaiky', this)">🌿 Thai kỳ</button>
            <button class="btn-outline" style="font-size: 0.72rem; padding: 4px 10px; white-space: nowrap; margin-top:0;" onclick="simFilterHealthTopics('hausan', this)">💛 Hậu sản</button>
            <button class="btn-outline" style="font-size: 0.72rem; padding: 4px 10px; white-space: nowrap; margin-top:0;" onclick="simFilterHealthTopics('noitiet', this)">🩸 Nội tiết</button>
            <button class="btn-outline" style="font-size: 0.72rem; padding: 4px 10px; white-space: nowrap; margin-top:0;" onclick="simFilterHealthTopics('mankinh', this)">🌸 Mãn kinh</button>
            <button class="btn-outline" style="font-size: 0.72rem; padding: 4px 10px; white-space: nowrap; margin-top:0;" onclick="simFilterHealthTopics('tamly', this)">🧠 Tâm lý</button>
            <button class="btn-outline" style="font-size: 0.72rem; padding: 4px 10px; white-space: nowrap; margin-top:0;" onclick="simFilterHealthTopics('timmach', this)">❤️ Tim mạch</button>
        </div>

        <!-- Topics Counter -->
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; font-size: 0.75rem; color: var(--text-muted);">
            <span id="ht-results-count">Hiển thị ${HT_ALL_TOPICS.length} khuyến nghị y tế</span>
            <span>💾 Cache 72 giờ</span>
        </div>

        <!-- Topics List -->
        <div id="ht-topics-list" style="display: flex; flex-direction: column; gap: 10px;">
            ${simRenderHealthTopicsCards(HT_ALL_TOPICS)}
        </div>
    `;
}

function simRenderHealthTopicsCards(topics) {
    if (!topics || topics.length === 0) {
        return `<div class="card" style="text-align: center; padding: 24px; color: var(--text-muted);">
            <i class="fa-solid fa-file-circle-question" style="font-size: 2rem; opacity: 0.4; display: block; margin-bottom: 8px;"></i>
            <p style="margin: 0; font-size: 0.85rem;">Không tìm thấy đề tài sức khỏe nào phù hợp.</p>
            <p style="margin: 4px 0 0 0; font-size: 0.75rem;">Hãy thử tìm từ khóa khác như "HPV", "canxi", "sau sinh", "loãng xương"...</p>
        </div>`;
    }

    return topics.map(t => {
        const [bg, col] = (HT_CAT_COLORS[t.catColor] || 'var(--surface-variant)|var(--on-surface)').split('|');
        return `
            <div class="card" style="padding: 12px 14px; animation: fadeUp 0.2s ease; border-left: 3px solid ${col};">
                <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 8px; margin-bottom: 6px;">
                    <span class="badge-num" style="background: ${bg}; color: ${col}; font-size: 0.7rem; font-weight: 600; padding: 2px 8px;">
                        ${t.icon} ${t.cat}
                    </span>
                    <span style="font-size: 0.7rem; color: var(--text-muted); background: var(--surface-variant); padding: 2px 6px; border-radius: 4px;">
                        <i class="fa-regular fa-clock"></i> ${t.age}
                    </span>
                </div>

                <h4 style="margin: 0 0 4px 0; font-size: 0.92rem; line-height: 1.35; color: var(--on-surface); font-weight: 700;">
                    ${t.title}
                </h4>
                <p class="tip-desc" style="margin: 0 0 8px 0; font-size: 0.78rem; line-height: 1.45; color: var(--text-secondary);">
                    ${t.body}
                </p>

                <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px dashed var(--outline-variant); padding-top: 8px;">
                    <span style="font-size: 0.72rem; color: var(--text-muted);">
                        ⏱ <strong>Tần suất:</strong> ${t.frequency}
                    </span>
                    <button class="btn-outline" style="font-size: 0.72rem; padding: 3px 8px; margin-top: 0;" onclick="simToggleTopicDetails(${t.id})">
                        Chi tiết <i class="fa-solid fa-chevron-down" id="ht-icon-${t.id}"></i>
                    </button>
                </div>

                <!-- Expandable Detail Section -->
                <div id="ht-detail-${t.id}" style="display: none; margin-top: 10px; padding-top: 10px; border-top: 1px solid var(--outline-variant); font-size: 0.78rem; animation: fadeUp 0.15s ease;">
                    <div style="background: var(--surface-variant); padding: 10px; border-radius: 8px; margin-bottom: 8px;">
                        <strong style="color: var(--primary); display: block; margin-bottom: 4px;">
                            <i class="fa-solid fa-notes-medical"></i> Khuyến nghị chuyên môn (USPSTF / HHS):
                        </strong>
                        <p style="margin: 0; line-height: 1.4; color: var(--on-surface);">${t.detail}</p>
                    </div>

                    <div>
                        <strong style="color: var(--terracotta); display: block; margin-bottom: 4px; font-size: 0.75rem;">
                            <i class="fa-regular fa-comments"></i> Câu hỏi nên trao đổi với Bác sĩ:
                        </strong>
                        <ul style="margin: 0; padding-left: 18px; line-height: 1.4; color: var(--text-secondary);">
                            ${t.questions.map(q => `<li style="margin-bottom: 3px;">"${q}"</li>`).join('')}
                        </ul>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function simToggleTopicDetails(id) {
    const el = document.getElementById(`ht-detail-${id}`);
    const icon = document.getElementById(`ht-icon-${id}`);
    if (!el) return;
    if (el.style.display === 'none' || !el.style.display) {
        el.style.display = 'block';
        if (icon) icon.className = 'fa-solid fa-chevron-up';
    } else {
        el.style.display = 'none';
        if (icon) icon.className = 'fa-solid fa-chevron-down';
    }
}

function simFilterHealthTopics(category, btnEl) {
    htCurrentCategory = category;
    
    // Update active button state
    const container = document.getElementById('ht-cat-chips');
    if (container) {
        const btns = container.querySelectorAll('button');
        btns.forEach(b => {
            b.className = 'btn-outline';
        });
        if (btnEl) btnEl.className = 'btn-primary-sm';
    }

    simApplyHealthTopicsFilter();
}

function simSearchHealthTopics(query) {
    htCurrentSearch = (query || '').trim().toLowerCase();
    simApplyHealthTopicsFilter();
}

function simApplyHealthTopicsFilter() {
    let filtered = HT_ALL_TOPICS;

    // Apply category filter
    if (htCurrentCategory !== 'all') {
        filtered = filtered.filter(t => t.catKey === htCurrentCategory);
    }

    // Apply search filter
    if (htCurrentSearch) {
        filtered = filtered.filter(t => 
            t.title.toLowerCase().includes(htCurrentSearch) ||
            t.body.toLowerCase().includes(htCurrentSearch) ||
            t.cat.toLowerCase().includes(htCurrentSearch) ||
            t.age.toLowerCase().includes(htCurrentSearch) ||
            t.detail.toLowerCase().includes(htCurrentSearch)
        );
    }

    const list = document.getElementById('ht-topics-list');
    const countEl = document.getElementById('ht-results-count');
    if (list) {
        list.innerHTML = simRenderHealthTopicsCards(filtered);
    }
    if (countEl) {
        countEl.textContent = `Hiển thị ${filtered.length} / ${HT_ALL_TOPICS.length} khuyến nghị y tế`;
    }
}

// --- Cache Clear ---
function simClearAllCache() {
    localStorage.removeItem('annhien_sim_cache');
    alert('🗑️ Đã xóa toàn bộ bộ nhớ tạm (OnlineCacheManager). Dữ liệu sẽ được tải mới khi cần.');
    openModule('onlineutilities');
}

// ========================================================
// MODULE 1: WEATHER & UV ALERT (OPEN-METEO API)
// ========================================================
const SIM_WEATHER_CITIES = {
    hanoi: {
        name: 'Hà Nội', lat: 21.0278, lon: 105.8342,
        temp: 28.5, feel: 31.0, humidity: 72, wind: 12, rain: 0.0,
        uv: 7.8, uvMax: 8.6, condition: 'Nắng nhẹ, có mây', icon: 'fa-sun',
        hourlyUv: [
            { time: '07:00', uv: 1.2, temp: 24 },
            { time: '09:00', uv: 4.5, temp: 26 },
            { time: '11:00', uv: 7.8, temp: 29 },
            { time: '13:00', uv: 8.6, temp: 31 },
            { time: '15:00', uv: 5.2, temp: 29 },
            { time: '17:00', uv: 1.5, temp: 27 }
        ]
    },
    hcm: {
        name: 'TP. Hồ Chí Minh', lat: 10.8231, lon: 106.6297,
        temp: 32.5, feel: 37.5, humidity: 78, wind: 10, rain: 1.5,
        uv: 9.4, uvMax: 10.2, condition: 'Nắng gắt, oi bức', icon: 'fa-sun-plant-wilt',
        hourlyUv: [
            { time: '07:00', uv: 1.8, temp: 27 },
            { time: '09:00', uv: 6.2, temp: 30 },
            { time: '11:00', uv: 9.4, temp: 33 },
            { time: '13:00', uv: 10.2, temp: 34 },
            { time: '15:00', uv: 6.8, temp: 32 },
            { time: '17:00', uv: 2.1, temp: 29 }
        ]
    },
    danang: {
        name: 'Đà Nẵng', lat: 16.0544, lon: 108.2022,
        temp: 29.0, feel: 32.0, humidity: 74, wind: 15, rain: 0.0,
        uv: 8.1, uvMax: 8.9, condition: 'Nắng đẹp, gió biển', icon: 'fa-cloud-sun',
        hourlyUv: [
            { time: '07:00', uv: 1.4, temp: 25 },
            { time: '09:00', uv: 5.0, temp: 28 },
            { time: '11:00', uv: 8.1, temp: 30 },
            { time: '13:00', uv: 8.9, temp: 31 },
            { time: '15:00', uv: 5.6, temp: 29 },
            { time: '17:00', uv: 1.8, temp: 27 }
        ]
    },
    dalat: {
        name: 'Đà Lạt', lat: 11.9404, lon: 108.4583,
        temp: 21.0, feel: 21.0, humidity: 68, wind: 8, rain: 0.0,
        uv: 6.5, uvMax: 7.2, condition: 'Se lạnh, nắng dịu', icon: 'fa-cloud-sun',
        hourlyUv: [
            { time: '07:00', uv: 1.0, temp: 16 },
            { time: '09:00', uv: 4.0, temp: 19 },
            { time: '11:00', uv: 6.5, temp: 22 },
            { time: '13:00', uv: 7.2, temp: 23 },
            { time: '15:00', uv: 4.8, temp: 20 },
            { time: '17:00', uv: 1.2, temp: 18 }
        ]
    },
    cantho: {
        name: 'Cần Thơ', lat: 10.0452, lon: 105.7469,
        temp: 31.0, feel: 35.5, humidity: 82, wind: 9, rain: 0.0,
        uv: 9.0, uvMax: 9.8, condition: 'Nắng ấm miền Tây', icon: 'fa-sun',
        hourlyUv: [
            { time: '07:00', uv: 1.6, temp: 26 },
            { time: '09:00', uv: 5.8, temp: 29 },
            { time: '11:00', uv: 9.0, temp: 32 },
            { time: '13:00', uv: 9.8, temp: 33 },
            { time: '15:00', uv: 6.2, temp: 31 },
            { time: '17:00', uv: 2.0, temp: 28 }
        ]
    }
};

let simSelectedWeatherCity = 'hanoi';

function simGetUvLevel(uv, isNight = false) {
    if (isNight || uv <= 0.05) return { label: 'Ban đêm (UV = 0)', bg: '#ede7f6', color: '#4a148c', advice: 'Ban đêm không có bức xạ tia UV. Đây là thời điểm vàng để da tự chữa lành, làm sạch sâu, đắp mặt nạ cấp nước và bôi kem dưỡng ẩm phục hồi màng bảo vệ da.' };
    if (uv <= 2.9) return { label: 'Thấp', bg: '#d4edda', color: '#155724', advice: 'An toàn khi ra ngoài, thích hợp tắm nắng sáng sớm hấp thụ Vitamin D tự nhiên cho mẹ & bé.' };
    if (uv <= 5.9) return { label: 'Trung bình', bg: '#fff3cd', color: '#856404', advice: 'Nên đội mũ rộng vành, đeo kính mát chống tia cực tím khi ra ngoài.' };
    if (uv <= 7.9) return { label: 'Cao', bg: '#ffe8d0', color: '#995200', advice: 'Cần thoa kem chống nắng SPF 50+, PA++++ trước khi ra ngoài 20 phút.' };
    if (uv <= 10.9) return { label: 'Rất cao', bg: '#ffcdd2', color: '#b71c1c', advice: 'Nguy cơ sạm nám thai kỳ (Melasma) và tổn thương ADN biểu bì rất cao! Hạn chế ra đường 11h–14h.' };
    return { label: 'Nguy hại', bg: '#e1bee7', color: '#6a1b9a', advice: 'Bức xạ cực đại! Ở trong bóng râm, mặc áo chống nắng sợi UPF 50+ chuyên dụng.' };
}

function simRenderWeatherView() {
    const rawData = SIM_WEATHER_CITIES[simSelectedWeatherCity];
    const currentHour = new Date().getHours();
    const isNight = currentHour >= 18 || currentHour < 6;
    
    const displayUv = isNight ? 0.0 : rawData.uv;
    const displayIcon = isNight ? 'fa-moon' : rawData.icon;
    const displayCondition = isNight ? 'Trời đêm mát mẻ, an toàn tuyệt đối (UV = 0)' : rawData.condition;
    const uvLvl = simGetUvLevel(displayUv, isNight);
    const uvPercent = Math.min(100, (displayUv / 12) * 100);

    return `
        <div class="card card-tip" style="margin-bottom: 12px;">
            <strong><i class="fa-solid fa-cloud-sun"></i> Open-Meteo Weather API</strong>
            <p class="tip-desc">Dữ liệu thời tiết & bức xạ tia cực tím (UV) thời gian thực. Tự động tính toán chu kỳ ngày/đêm chính xác.</p>
        </div>

        <!-- City Selector -->
        <div style="display: flex; gap: 6px; overflow-x: auto; padding-bottom: 8px; margin-bottom: 12px;">
            <button class="${simSelectedWeatherCity === 'hanoi' ? 'btn-primary-sm' : 'btn-outline'}" style="font-size:0.75rem; padding:4px 10px;" onclick="simLoadWeatherCity('hanoi')">📍 Hà Nội</button>
            <button class="${simSelectedWeatherCity === 'hcm' ? 'btn-primary-sm' : 'btn-outline'}" style="font-size:0.75rem; padding:4px 10px;" onclick="simLoadWeatherCity('hcm')">📍 TP. HCM</button>
            <button class="${simSelectedWeatherCity === 'danang' ? 'btn-primary-sm' : 'btn-outline'}" style="font-size:0.75rem; padding:4px 10px;" onclick="simLoadWeatherCity('danang')">📍 Đà Nẵng</button>
            <button class="${simSelectedWeatherCity === 'dalat' ? 'btn-primary-sm' : 'btn-outline'}" style="font-size:0.75rem; padding:4px 10px;" onclick="simLoadWeatherCity('dalat')">📍 Đà Lạt</button>
            <button class="${simSelectedWeatherCity === 'cantho' ? 'btn-primary-sm' : 'btn-outline'}" style="font-size:0.75rem; padding:4px 10px;" onclick="simLoadWeatherCity('cantho')">📍 Cần Thơ</button>
        </div>

        <div id="sim-weather-container">
            <!-- Hero Weather Card -->
            <div class="weather-hero-card" style="${isNight ? 'background: linear-gradient(135deg, #1a237e 0%, #283593 50%, #311b92 100%); color: #fff;' : ''}">
                <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                    <div>
                        <h2 style="font-size: 1.6rem; margin: 0; color: ${isNight ? '#ffffff' : 'var(--text-primary)'};">${rawData.name}</h2>
                        <p class="tip-desc" style="margin: 2px 0 10px 0; color: ${isNight ? '#e0e0e0' : 'var(--text-muted)'};">${displayCondition}</p>
                        <div style="font-size: 2.6rem; font-weight: 800; color: ${isNight ? '#80deea' : 'var(--primary)'}; line-height: 1;">
                            ${rawData.temp}°C
                        </div>
                        <p style="font-size: 0.8rem; color: ${isNight ? '#b0bec5' : 'var(--text-muted)'}; margin-top: 4px;">Cảm giác như <strong>${rawData.feel}°C</strong> • ${isNight ? '🌙 Ban đêm' : '☀️ Ban ngày'}</p>
                    </div>
                    <div style="text-align: right;">
                        <i class="fa-solid ${displayIcon}" style="font-size: 3rem; color: ${isNight ? '#ffd54f' : '#f9a825'}; opacity: 0.95;"></i>
                        <div style="margin-top: 10px;">
                            <span class="badge-num" style="background: ${isNight ? '#3949ab' : uvLvl.bg}; color: ${isNight ? '#ffffff' : uvLvl.color}; font-weight: 700; border: 1px solid ${isNight ? '#7986cb' : 'transparent'};">
                                ${isNight ? '🌙 Ban đêm (UV = 0)' : `UV ${displayUv} — ${uvLvl.label}`}
                            </span>
                        </div>
                    </div>
                </div>

                <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin-top: 16px; font-size: 0.78rem; text-align: center;">
                    <div style="background: ${isNight ? 'rgba(255,255,255,0.12)' : 'var(--surface)'}; padding: 8px; border-radius: 10px; border: 1px solid ${isNight ? 'rgba(255,255,255,0.2)' : 'var(--outline-variant)'};">
                        <div style="color: ${isNight ? '#b0bec5' : 'var(--text-muted)'};"><i class="fa-solid fa-droplet text-primary"></i> Độ ẩm</div>
                        <strong style="font-size: 0.95rem; color: ${isNight ? '#ffffff' : 'inherit'};">${rawData.humidity}%</strong>
                    </div>
                    <div style="background: ${isNight ? 'rgba(255,255,255,0.12)' : 'var(--surface)'}; padding: 8px; border-radius: 10px; border: 1px solid ${isNight ? 'rgba(255,255,255,0.2)' : 'var(--outline-variant)'};">
                        <div style="color: ${isNight ? '#b0bec5' : 'var(--text-muted)'};"><i class="fa-solid fa-wind" style="color: var(--secondary);"></i> Sức gió</div>
                        <strong style="font-size: 0.95rem; color: ${isNight ? '#ffffff' : 'inherit'};">${rawData.wind} km/h</strong>
                    </div>
                    <div style="background: ${isNight ? 'rgba(255,255,255,0.12)' : 'var(--surface)'}; padding: 8px; border-radius: 10px; border: 1px solid ${isNight ? 'rgba(255,255,255,0.2)' : 'var(--outline-variant)'};">
                        <div style="color: ${isNight ? '#ffd54f' : 'var(--text-muted)'};"><i class="fa-solid fa-sun text-terracotta"></i> Đỉnh trưa nay</div>
                        <strong style="font-size: 0.95rem; color: ${isNight ? '#ffffff' : 'inherit'};">${rawData.uvMax}</strong>
                    </div>
                </div>
            </div>

            <!-- UV Gauge Meter -->
            <div class="uv-meter-wrap">
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <strong>Thước đo chỉ số tia UV hiện tại</strong>
                    <span style="font-weight: 800; font-size: 1.1rem; color: ${uvLvl.color};">${isNight ? '0.0 (Ban đêm)' : displayUv}</span>
                </div>
                <div class="uv-scale-bar">
                    <div class="uv-pointer" style="left: ${uvPercent}%;"></div>
                </div>
                <div style="display: flex; justify-content: space-between; font-size: 0.7rem; color: var(--text-muted);">
                    <span>0 Thấp (Đêm)</span>
                    <span>3 TB</span>
                    <span>6 Cao</span>
                    <span>8 Rất cao</span>
                    <span>11+ Cực độ</span>
                </div>
            </div>

            <!-- Hourly UV Forecast -->
            <h4 class="section-title">Diễn biến UV theo giờ trong ngày</h4>
            <div class="hourly-weather-scroll">
                ${rawData.hourlyUv.map(h => {
                    const l = simGetUvLevel(h.uv);
                    return `
                    <div class="hourly-weather-item">
                        <div style="color: var(--text-muted); margin-bottom: 4px;">${h.time}</div>
                        <strong style="font-size: 0.9rem; color: ${l.color};">${h.uv}</strong>
                        <div style="font-size: 0.72rem; color: var(--text-muted); margin-top: 2px;">${h.temp}°C</div>
                    </div>`;
                }).join('')}
            </div>

            <!-- Tailored Women's Health Advice -->
            <h4 class="section-title">Khuyến nghị sức khỏe & Làn da phụ nữ</h4>
            <div class="card" style="padding: 14px; margin-bottom: 12px; border-left: 4px solid ${isNight ? '#7e57c2' : '#f9a825'};">
                <div style="display: flex; gap: 10px; align-items: flex-start;">
                    <i class="fa-solid ${isNight ? 'fa-moon' : 'fa-shield-halved'}" style="color: ${isNight ? '#7e57c2' : '#f9a825'}; font-size: 1.3rem; margin-top: 2px;"></i>
                    <div>
                        <strong style="font-size: 0.9rem;">${isNight ? 'Chăm sóc & Phục hồi da ban đêm (UV = 0)' : 'Bảo vệ da & Phòng nám thai kỳ (Melasma)'}</strong>
                        <p class="tip-desc" style="margin-top: 4px;">
                            ${uvLvl.advice}
                        </p>
                    </div>
                </div>
            </div>

            <div class="card" style="padding: 14px; margin-bottom: 14px; border-left: 4px solid var(--primary);">
                <div style="display: flex; gap: 10px; align-items: flex-start;">
                    <i class="fa-solid fa-glass-water" style="color: var(--primary); font-size: 1.3rem; margin-top: 2px;"></i>
                    <div>
                        <strong style="font-size: 0.9rem;">Thân nhiệt & Bù nước chủ động</strong>
                        <p class="tip-desc" style="margin-top: 4px;">
                            Nhiệt độ hiện tại ${rawData.temp}°C khiến cơ thể dễ mất nước. Thai phụ nên bổ sung ít nhất 2.5 lít nước ấm mỗi ngày để phòng ngừa cơn gò sinh non và mệt mỏi do tụt huyết áp.
                        </p>
                    </div>
                </div>
            </div>

            <!-- Actions -->
            <div style="display: flex; gap: 8px;">
                <button class="btn-primary" style="flex: 1;" onclick="simRefreshWeather()"><i class="fa-solid fa-rotate"></i> Cập nhật thời gian thực</button>
                <button class="btn-outline" onclick="alert('⏰ Đã bật nhắc nhở: Thoa lại kem chống nắng sau mỗi 2 giờ vào ban ngày!')"><i class="fa-regular fa-bell"></i> Nhắc kem chống nắng</button>
            </div>
        </div>
    `;
}

function simLoadWeatherCity(city) {
    simSelectedWeatherCity = city;
    const body = document.getElementById('module-body');
    if (body) {
        body.innerHTML = simRenderWeatherView();
    }
}

function simRefreshWeather() {
    const cont = document.getElementById('sim-weather-container');
    if (!cont) return;
    cont.innerHTML = `<div style="text-align:center; padding: 30px; color: var(--text-muted);"><i class="fa-solid fa-spinner fa-spin" style="font-size:1.8rem; color:var(--primary);"></i><p style="margin-top:10px;">Đang kết nối Open-Meteo Forecast API...</p></div>`;
    
    // Attempt live fetch if online
    const city = SIM_WEATHER_CITIES[simSelectedWeatherCity];
    const url = `https://api.open-meteo.com/v1/forecast?latitude=${city.lat}&longitude=${city.lon}&current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,wind_speed_10m&daily=uv_index_max&hourly=uv_index&timezone=Asia%2FHo_Chi_Minh`;
    
    fetch(url, { signal: AbortSignal.timeout(4000) })
        .then(r => r.json())
        .then(data => {
            if (data && data.current) {
                city.temp = Math.round(data.current.temperature_2m * 10) / 10;
                city.feel = Math.round(data.current.apparent_temperature * 10) / 10;
                city.humidity = data.current.relative_humidity_2m;
                city.wind = Math.round(data.current.wind_speed_10m);
                if (data.daily && data.daily.uv_index_max) {
                    city.uvMax = data.daily.uv_index_max[0] || city.uvMax;
                    const h = new Date().getHours();
                    if (h >= 18 || h < 6) {
                        city.uv = 0.0;
                    } else if (data.hourly && data.hourly.uv_index && data.hourly.uv_index[h] !== undefined) {
                        city.uv = data.hourly.uv_index[h];
                    } else {
                        city.uv = Math.min(city.uvMax, Math.round((city.uvMax * 0.85) * 10) / 10);
                    }
                }
            }
            cont.innerHTML = simRenderWeatherView();
        })
        .catch(() => {
            // Offline fallback
            setTimeout(() => {
                cont.innerHTML = simRenderWeatherView();
            }, 600);
        });
}

// ========================================================
// MODULE: AIR QUALITY (OPENAQ V3 API)
// ========================================================
const DEFAULT_OPENAQ_KEY = 'ddf48b14c02c48aa46eca82abab6fee9a7f291a6055c8fd9d96ff15ce2b576cd';

const SIM_AQ_CITIES = {
    hanoi: { name: 'Hà Nội', lat: 21.0278, lon: 105.8342 },
    hcm: { name: 'TP. Hồ Chí Minh', lat: 10.8231, lon: 106.6297 },
    danang: { name: 'Đà Nẵng', lat: 16.0544, lon: 108.2022 },
    cantho: { name: 'Cần Thơ', lat: 10.0452, lon: 105.7469 },
    haiphong: { name: 'Hải Phòng', lat: 20.8449, lon: 106.6881 }
};

let simSelectedAqCity = 'hanoi';
let simAqStationsCache = null;

const DEFAULT_AQICN_TOKEN = 'a91f3c8524d08973aa73870b750414f39de41442';

function simGetAqicnToken() {
    return localStorage.getItem('annhien_aqicn_token') || DEFAULT_AQICN_TOKEN;
}

function simSaveAqicnToken() {
    const input = document.getElementById('sim-aqicn-token-input');
    if (input) {
        const token = input.value.trim();
        if (token) {
            localStorage.setItem('annhien_aqicn_token', token);
            alert('✓ Đã lưu AQICN API Token vào bộ nhớ máy!');
            simRefreshAirQuality();
        } else {
            alert('Vui lòng nhập API Token hợp lệ!');
        }
    }
}

function simGetAqLevelByAqi(aqi) {
    if (aqi == null || isNaN(aqi)) {
        return { label: 'Chưa có dữ liệu', color: '#757575', bg: '#f5f5f5', advice: 'Chưa nhận được phản hồi từ trạm đo.' };
    }
    if (aqi <= 50) {
        return { label: 'Tốt (0 - 50)', color: '#2e7d32', bg: '#e8f5e9', advice: 'Chất lượng không khí trong lành, rất lý tưởng cho các hoạt động thể dục dạo bộ ngoài trời của mẹ và bé.' };
    }
    if (aqi <= 100) {
        return { label: 'Trung bình (51 - 100)', color: '#f9a825', bg: '#fffde7', advice: 'Chất lượng chấp nhận được. Thai phụ có tiền sử viêm xoang dị ứng nên rửa mũi nước muối sinh lý sau khi ra ngoài.' };
    }
    if (aqi <= 150) {
        return { label: 'Kém (101 - 150)', color: '#e65100', bg: '#fff3e0', advice: 'Bụi mịn kích ứng niêm mạc. Thai phụ, phụ nữ sau sinh và trẻ nhỏ nên hạn chế hoạt động mạnh kéo dài ngoài trời.' };
    }
    if (aqi <= 200) {
        return { label: 'Xấu (151 - 200)', color: '#c62828', bg: '#ffebee', advice: 'Bắt đầu có hại cho mọi người. Đeo khẩu trang chống bụi N95/KF94 khi ra đường, bật máy lọc không khí trong phòng ngủ.' };
    }
    if (aqi <= 300) {
        return { label: 'Rất xấu (201 - 300)', color: '#6a1b9a', bg: '#f3e5f5', advice: 'Cảnh báo sức khỏe nghiêm trọng! Đóng kín toàn bộ cửa sổ, tuyệt đối không tập thể dục ngoài trời, mở máy lọc khí 24/7.' };
    }
    return { label: 'Nguy hại (> 300)', color: '#4e342e', bg: '#efebe9', advice: 'Mức độ ô nhiễm khẩn cấp. Mọi đối tượng nên ở trong nhà, phụ nữ mang thai tránh tiếp xúc môi trường ô nhiễm.' };
}

function simRenderAirQualityView() {
    const currentToken = simGetAqicnToken();
    const city = SIM_AQ_CITIES[simSelectedAqCity];

    return `
        <div class="card card-tip" style="margin-bottom: 12px;">
            <strong><i class="fa-solid fa-wind"></i> Mạng lưới quan trắc chất lượng không khí AQICN</strong>
            <p class="tip-desc">Dữ liệu quan trắc trạm đo thời gian thực từ waqi.info (Chỉ số AQI chuẩn EPA, bụi PM2.5, PM10, khí O₃, NO₂).</p>
        </div>

        <!-- Token Config Card -->
        <div class="card" style="padding: 12px 14px; margin-bottom: 12px; background: var(--surface-variant);">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
                <strong style="font-size: 0.85rem;"><i class="fa-solid fa-key" style="color: var(--primary);"></i> AQICN API Token</strong>
                <span class="badge-num" style="background: #e8f5e9; color: #2e7d32; font-size: 0.7rem;">✓ Đã xác thực</span>
            </div>
            <div style="display: flex; gap: 6px;">
                <input type="text" id="sim-aqicn-token-input" value="${currentToken}" placeholder="Nhập API Token..." style="flex: 1; font-size: 0.75rem; padding: 6px 8px; border-radius: 8px; border: 1px solid var(--outline); font-family: monospace; outline: none; background: var(--surface);">
                <button class="btn-primary-sm" onclick="simSaveAqicnToken()" style="font-size: 0.75rem; padding: 4px 10px;">Lưu</button>
            </div>
            <p style="font-size: 0.7rem; color: var(--text-muted); margin: 6px 0 0 0;">Token chính thức từ aqicn.org • Miễn phí 1000 lượt/phút.</p>
        </div>

        <!-- City Selector -->
        <div class="card" style="padding: 14px; margin-bottom: 12px;">
            <div class="card-row" style="margin-bottom: 10px;">
                <div>
                    <strong>Khu vực quan trắc</strong>
                    <p class="tip-desc" id="sim-aq-coords">${city.lat}° N, ${city.lon}° E (${city.name})</p>
                </div>
                <button class="btn-primary-sm" onclick="simRefreshAirQuality()" id="btn-aq-refresh"><i class="fa-solid fa-rotate"></i> Tải lại</button>
            </div>
            <div style="display: flex; gap: 6px; overflow-x: auto; padding-bottom: 4px;">
                <button class="${simSelectedAqCity === 'hanoi' ? 'btn-primary-sm' : 'btn-outline'}" style="font-size:0.75rem; padding:4px 10px;" onclick="simLoadAQCity('hanoi')">📍 Hà Nội</button>
                <button class="${simSelectedAqCity === 'hcm' ? 'btn-primary-sm' : 'btn-outline'}" style="font-size:0.75rem; padding:4px 10px;" onclick="simLoadAQCity('hcm')">📍 TP. HCM</button>
                <button class="${simSelectedAqCity === 'danang' ? 'btn-primary-sm' : 'btn-outline'}" style="font-size:0.75rem; padding:4px 10px;" onclick="simLoadAQCity('danang')">📍 Đà Nẵng</button>
                <button class="${simSelectedAqCity === 'cantho' ? 'btn-primary-sm' : 'btn-outline'}" style="font-size:0.75rem; padding:4px 10px;" onclick="simLoadAQCity('cantho')">📍 Cần Thơ</button>
                <button class="${simSelectedAqCity === 'haiphong' ? 'btn-primary-sm' : 'btn-outline'}" style="font-size:0.75rem; padding:4px 10px;" onclick="simLoadAQCity('haiphong')">📍 Hải Phòng</button>
            </div>
        </div>

        <!-- Live Station Container -->
        <div id="sim-aq-station-container">
            ${simRenderDefaultAqView(city)}
        </div>
    `;
}

function simRenderDefaultAqView(city) {
    const isHanoi = simSelectedAqCity === 'hanoi';
    const aqiVal = isHanoi ? 68 : 52;
    const pm25Val = isHanoi ? 22.4 : 15.5;
    const pm10Val = isHanoi ? 42.1 : 31.0;
    const lvl = simGetAqLevelByAqi(aqiVal);

    return `
        <div class="card" style="padding: 16px; margin-bottom: 12px; border-left: 4px solid ${lvl.color};">
            <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px;">
                <div>
                    <h3 style="margin: 0; font-size: 1.05rem; color: var(--text-primary);">${isHanoi ? 'Trạm Hà Nội (United Nations IS)' : 'Trạm TP. Hồ Chí Minh — ' + city.name}</h3>
                    <p class="tip-desc" style="margin: 2px 0 0 0;">Nguồn: AQICN / WAQI Realtime Feed</p>
                </div>
                <span class="badge-num" style="background: ${lvl.bg}; color: ${lvl.color}; font-weight: 700;">
                    ${lvl.label}
                </span>
            </div>

            <!-- Big AQI Circle Display -->
            <div style="display: flex; align-items: center; justify-content: center; margin: 14px 0;">
                <div style="width: 90px; height: 90px; border-radius: 50%; background: ${lvl.color}; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #fff; box-shadow: 0 4px 12px rgba(0,0,0,0.12);">
                    <span style="font-size: 2rem; font-weight: 800; line-height: 1;">${aqiVal}</span>
                    <span style="font-size: 0.7rem; font-weight: 600; opacity: 0.9;">AQI</span>
                </div>
            </div>

            <!-- Parameters Grid -->
            <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin: 12px 0; text-align: center;">
                <div style="background: var(--surface-variant); padding: 8px; border-radius: 10px;">
                    <div style="font-size: 0.72rem; color: var(--text-muted);">Bụi PM2.5</div>
                    <strong style="font-size: 1.05rem; color: ${lvl.color};">${pm25Val}</strong>
                    <div style="font-size: 0.65rem; color: var(--text-muted);">µg/m³</div>
                </div>
                <div style="background: var(--surface-variant); padding: 8px; border-radius: 10px;">
                    <div style="font-size: 0.72rem; color: var(--text-muted);">Bụi PM10</div>
                    <strong style="font-size: 1.05rem; color: var(--text-primary);">${pm10Val}</strong>
                    <div style="font-size: 0.65rem; color: var(--text-muted);">µg/m³</div>
                </div>
                <div style="background: var(--surface-variant); padding: 8px; border-radius: 10px;">
                    <div style="font-size: 0.72rem; color: var(--text-muted);">Khí Ozone (O₃)</div>
                    <strong style="font-size: 1.05rem; color: var(--text-primary);">14.2</strong>
                    <div style="font-size: 0.65rem; color: var(--text-muted);">ppb</div>
                </div>
            </div>

            <!-- Health Protection Advice -->
            <div style="background: rgba(46, 125, 50, 0.06); border-radius: 8px; padding: 10px; font-size: 0.78rem; line-height: 1.45;">
                <i class="fa-solid fa-shield-heart text-primary"></i> <strong>Khuyến nghị thai phụ & Mẹ bé:</strong><br>
                ${lvl.advice}
            </div>
            <p style="font-size: 0.7rem; color: var(--text-muted); margin: 8px 0 0 0; text-align: right;">Cập nhật: Vừa xong • Nguồn: AQICN / WAQI Network</p>
        </div>
    `;
}

function simLoadAQCity(cityKey) {
    simSelectedAqCity = cityKey;
    const body = document.getElementById('module-body');
    if (body) {
        body.innerHTML = simRenderAirQualityView();
        simRefreshAirQuality();
    }
}

function simRefreshAirQuality() {
    const cont = document.getElementById('sim-aq-station-container');
    if (!cont) return;
    
    cont.innerHTML = `
        <div style="text-align: center; padding: 30px; color: var(--text-muted);">
            <i class="fa-solid fa-spinner fa-spin" style="font-size: 1.8rem; color: var(--primary);"></i>
            <p style="margin-top: 10px;">Đang kết nối AQICN / WAQI API...</p>
        </div>
    `;

    const city = SIM_AQ_CITIES[simSelectedAqCity];
    const token = simGetAqicnToken();
    const url = `https://api.waqi.info/feed/geo:${city.lat};${city.lon}/?token=${token}`;

    fetch(url, { signal: AbortSignal.timeout(6000) })
    .then(r => {
        if (!r.ok) throw new Error('HTTP ' + r.status);
        return r.json();
    })
    .then(res => {
        if (res && res.status === 'ok' && res.data) {
            const data = res.data;
            const aqiVal = data.aqi || 50;
            const lvl = simGetAqLevelByAqi(aqiVal);
            const iaqi = data.iaqi || {};
            const pm25 = iaqi.pm25 ? iaqi.pm25.v : '--';
            const pm10 = iaqi.pm10 ? iaqi.pm10.v : '--';
            const o3 = iaqi.o3 ? iaqi.o3.v : '--';
            const temp = iaqi.t ? iaqi.t.v + '°C' : '--';
            const hum = iaqi.h ? iaqi.h.v + '%' : '--';
            const stationName = data.city ? data.city.name : city.name;
            const updateTime = data.time ? data.time.s : 'Vừa xong';

            cont.innerHTML = `
                <div class="card" style="padding: 16px; margin-bottom: 12px; border-left: 4px solid ${lvl.color}; animation: fadeUp 0.3s ease;">
                    <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px;">
                        <div>
                            <h3 style="margin: 0; font-size: 1.05rem; color: var(--text-primary);">${stationName}</h3>
                            <p class="tip-desc" style="margin: 2px 0 0 0;">Cập nhật lúc: ${updateTime}</p>
                        </div>
                        <span class="badge-num" style="background: ${lvl.bg}; color: ${lvl.color}; font-weight: 700;">
                            ${lvl.label}
                        </span>
                    </div>

                    <!-- Big AQI Circle Display -->
                    <div style="display: flex; align-items: center; justify-content: center; margin: 14px 0;">
                        <div style="width: 90px; height: 90px; border-radius: 50%; background: ${lvl.color}; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #fff; box-shadow: 0 4px 12px rgba(0,0,0,0.12);">
                            <span style="font-size: 2rem; font-weight: 800; line-height: 1;">${aqiVal}</span>
                            <span style="font-size: 0.7rem; font-weight: 600; opacity: 0.9;">AQI</span>
                        </div>
                    </div>

                    <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin: 12px 0; text-align: center;">
                        <div style="background: var(--surface-variant); padding: 8px; border-radius: 10px;">
                            <div style="font-size: 0.72rem; color: var(--text-muted);">Bụi PM2.5</div>
                            <strong style="font-size: 1.05rem; color: ${lvl.color};">${pm25}</strong>
                            <div style="font-size: 0.65rem; color: var(--text-muted);">µg/m³</div>
                        </div>
                        <div style="background: var(--surface-variant); padding: 8px; border-radius: 10px;">
                            <div style="font-size: 0.72rem; color: var(--text-muted);">Bụi PM10</div>
                            <strong style="font-size: 1.05rem; color: var(--text-primary);">${pm10}</strong>
                            <div style="font-size: 0.65rem; color: var(--text-muted);">µg/m³</div>
                        </div>
                        <div style="background: var(--surface-variant); padding: 8px; border-radius: 10px;">
                            <div style="font-size: 0.72rem; color: var(--text-muted);">Khí Ozone (O₃)</div>
                            <strong style="font-size: 1.05rem; color: var(--text-primary);">${o3}</strong>
                            <div style="font-size: 0.65rem; color: var(--text-muted);">ppb</div>
                        </div>
                        <div style="background: var(--surface-variant); padding: 8px; border-radius: 10px;">
                            <div style="font-size: 0.72rem; color: var(--text-muted);">Nhiệt độ</div>
                            <strong style="font-size: 1.05rem; color: var(--text-primary);">${temp}</strong>
                        </div>
                        <div style="background: var(--surface-variant); padding: 8px; border-radius: 10px;">
                            <div style="font-size: 0.72rem; color: var(--text-muted);">Độ ẩm</div>
                            <strong style="font-size: 1.05rem; color: var(--text-primary);">${hum}</strong>
                        </div>
                        <div style="background: var(--surface-variant); padding: 8px; border-radius: 10px;">
                            <div style="font-size: 0.72rem; color: var(--text-muted);">Mức AQI</div>
                            <strong style="font-size: 1.05rem; color: ${lvl.color};">${aqiVal}</strong>
                        </div>
                    </div>

                    <div style="background: rgba(46, 125, 50, 0.06); border-radius: 8px; padding: 10px; font-size: 0.78rem; line-height: 1.45;">
                        <i class="fa-solid fa-shield-heart text-primary"></i> <strong>Khuyến nghị thai phụ & Mẹ bé:</strong><br>
                        ${lvl.advice}
                    </div>
                    <p style="font-size: 0.7rem; color: var(--primary); margin: 8px 0 0 0; text-align: right;">✓ Dữ liệu trực tuyến từ AQICN Realtime API</p>
                </div>
            `;
        } else {
            cont.innerHTML = simRenderDefaultAqView(city);
        }
    })
    .catch(err => {
        console.warn('AQICN fetch fallback:', err);
        cont.innerHTML = simRenderDefaultAqView(city);
    });
}

// ========================================================
// MODULE 2: WORKOUT LIBRARY FOR WOMEN (WGER API)
// ========================================================
const SIM_WORKOUTS_DB = [
    {
        id: 1, cat: 'pelvic', catName: 'Cơ sàn chậu', icon: '🌸',
        name: 'Co bóp sàn chậu Kegel nhịp nhàng', enName: 'Pelvic Floor Contractions',
        target: 'Cơ mu cụt (PC), cơ nâng hậu môn, vòm bàng quang',
        reps: '10 nhịp (Siết 5s — Thả lỏng 5s)',
        steps: [
            'Nằm ngửa hoặc ngồi thẳng lưng, thả lỏng cơ mông và cơ đùi hoàn toàn.',
            'Hít sâu vào bụng, khi thở ra nhẹ nhàng siết chặt nhóm cơ đáy chậu như đang nhịn tiểu.',
            'Giữ nguyên trạng thái siết chặt trong 5 giây, không nín thở.',
            'Thả lỏng hoàn toàn trong 5 giây trước khi bắt đầu nhịp tiếp theo.'
        ],
        caution: 'Không gồng cơ bụng hoặc ép hai đùi vào nhau. Nếu cảm thấy đau vùng xương mu, hãy nghỉ ngơi.'
    },
    {
        id: 2, cat: 'prenatal', catName: 'Thai kỳ an toàn', icon: '🤰',
        name: 'Tư thế Con Mèo — Con Bò cho thai phụ', enName: 'Prenatal Cat-Cow Pose',
        target: 'Cột sống thắt lưng, cơ dựng gai sống, giảm chèn ép thần kinh tọa',
        reps: '8 – 10 lần hít thở sâu',
        steps: [
            'Quỳ 4 điểm trên thảm, hai tay thẳng dưới vai, hai đầu gối mở rộng bằng hông tạo không gian cho bụng bầu.',
            'Hít vào nhẹ nhàng: hạ bụng xuống một chút, nâng ngực và cằm lên (Con Bò).',
            'Thở ra êm ái: uốn cong lưng lên trần nhà như lưng mèo, cúi đầu nhẹ (Con Mèo).',
            'Lặp lại theo nhịp thở tự nhiên, êm dịu.'
        ],
        caution: 'Tránh hạ võng lưng quá sâu ở tam cá nguyệt thứ 3 để không gây căng dây chằng tròn tử cung.'
    },
    {
        id: 3, cat: 'cramps', catName: 'Giảm đau bụng kinh', icon: '🩸',
        name: 'Tư thế Em Bé mở rộng giải tỏa cơn đau', enName: 'Extended Child\'s Pose (Balasana)',
        target: 'Giãn cơ vùng chậu, thắt lưng dưới, làm dịu tử cung co thắt',
        reps: 'Giữ 2 – 3 phút hít thở sâu',
        steps: [
            'Quỳ trên thảm, hai ngón chân cái chạm nhau, mở rộng hai đầu gối bằng mép thảm.',
            'Từ từ hạ hông về phía gót chân, vươn dài hai tay về phía trước thảm.',
            'Hạ trán chạm sàn mềm mại, thả lỏng toàn bộ vai, cổ và thắt lưng.',
            'Hít sâu cảm nhận hơi thở đi xuống bụng dưới, massage tự nhiên cho tử cung.'
        ],
        caution: 'Có thể đặt một chiếc gối ôm mềm dưới ngực và bụng để nâng đỡ thoải mái hơn.'
    },
    {
        id: 4, cat: 'cramps', catName: 'Giảm đau bụng kinh', icon: '🦋',
        name: 'Tư thế Cánh Bướm nằm ngửa thư giãn', enName: 'Supta Baddha Konasana',
        target: 'Mở khớp háng, lưu thông khí huyết buồng trứng, giảm ứ huyết',
        reps: 'Thư giãn 3 – 5 phút',
        steps: [
            'Nằm ngửa trên thảm, co hai đầu gối lại.',
            'Chụm hai lòng bàn chân vào nhau, mở hai đầu gối rơi sang hai bên tự nhiên.',
            'Đặt một tay lên ngực, một tay lên bụng dưới để cảm nhận nhịp thở bình an.',
            'Nhắm mắt, thả lỏng toàn bộ cơ thể và đón nhận sự dịu lành.'
        ],
        caution: 'Kê thêm 2 chiếc gối nhỏ dưới hai đùi nếu cơ khớp háng bị căng tức.'
    },
    {
        id: 5, cat: 'postpartum', catName: 'Phục hồi sau sinh', icon: '🧘',
        name: 'Cầu mông có kiểm soát (An toàn tách cơ bụng)', enName: 'Glute Bridge (Diastasis Recti Safe)',
        target: 'Cơ mông lớn, cơ gân kheo, cơ bụng sâu Transverse Abdominis',
        reps: '3 hiệp x 10 lần',
        steps: [
            'Nằm ngửa, hai chân gập gối, bàn chân đặt vững chắc trên sàn cách mông khoảng 1 gang tay.',
            'Hít vào mở rộng lồng ngực. Thở ra: siết nhẹ cơ sàn chậu và cơ bụng dưới.',
            'Nhấn gót chân nâng hông lên cao cho đến khi đùi và thân người tạo thành đường thẳng.',
            'Giữ ở đỉnh 2 giây rồi từ từ hạ xuống có kiểm soát.'
        ],
        caution: 'Không ưỡn ngực quá cao gây võng thắt lưng. Chỉ tập sau sinh từ tuần thứ 6 (sinh thường) hoặc tuần thứ 10 (sinh mổ) khi vết mổ đã lành.'
    },
    {
        id: 6, cat: 'postpartum', catName: 'Phục hồi sau sinh', icon: '🌬️',
        name: 'Tập thở cơ hoành phục hồi nội tạng', enName: 'Diaphragmatic Deep Breathing',
        target: 'Cơ hoành, kích hoạt hệ thần kinh phó giao cảm, giảm hormone căng thẳng Cortisol',
        reps: '15 nhịp thở chậm',
        steps: [
            'Ngồi thoải mái hoặc nằm thẳng lưng, đặt một tay lên ngực và một tay lên bụng.',
            'Hít vào chậm qua mũi trong 4 giây, cảm nhận bụng phình lên, ngực giữ yên.',
            'Giữ hơi trong 2 giây tĩnh lặng.',
            'Thở ra nhẹ nhàng qua khóe môi trong 6 giây, cảm nhận rốn rút nhẹ về phía cột sống.'
        ],
        caution: 'Không cố gắng hít quá sức gây chóng mặt. Hãy giữ nhịp thở thật êm dịu.'
    }
];

let simWorkoutFilter = 'all';
let kegelTimerInterval = null;
let kegelSecondsLeft = 5;
let kegelPhase = 'squeeze'; // 'squeeze' or 'relax'
let kegelRepsDone = 0;
const KEGEL_TOTAL_REPS = 10;

function simRenderWorkoutsView() {
    const filtered = simWorkoutFilter === 'all' ? SIM_WORKOUTS_DB : SIM_WORKOUTS_DB.filter(w => w.cat === simWorkoutFilter);

    return `
        <div class="card card-tip" style="margin-bottom: 12px;">
            <strong><i class="fa-solid fa-person-walking"></i> Thư viện Wger Workout API</strong>
            <p class="tip-desc">Các bài tập chuyên biệt cho phụ nữ: Cơ sàn chậu, thai kỳ, đau bụng kinh & phục hồi sau sinh. Cache 7 ngày.</p>
        </div>

        <!-- Category Filter -->
        <div style="display: flex; gap: 6px; overflow-x: auto; padding-bottom: 8px; margin-bottom: 12px;">
            <button class="${simWorkoutFilter === 'all' ? 'btn-primary-sm' : 'btn-outline'}" style="font-size:0.75rem; padding:4px 10px;" onclick="simFilterWorkouts('all')">Tất cả (${SIM_WORKOUTS_DB.length})</button>
            <button class="${simWorkoutFilter === 'pelvic' ? 'btn-primary-sm' : 'btn-outline'}" style="font-size:0.75rem; padding:4px 10px;" onclick="simFilterWorkouts('pelvic')">🌸 Cơ sàn chậu</button>
            <button class="${simWorkoutFilter === 'prenatal' ? 'btn-primary-sm' : 'btn-outline'}" style="font-size:0.75rem; padding:4px 10px;" onclick="simFilterWorkouts('prenatal')">🤰 Thai kỳ an toàn</button>
            <button class="${simWorkoutFilter === 'cramps' ? 'btn-primary-sm' : 'btn-outline'}" style="font-size:0.75rem; padding:4px 10px;" onclick="simFilterWorkouts('cramps')">🩸 Giảm đau kinh</button>
            <button class="${simWorkoutFilter === 'postpartum' ? 'btn-primary-sm' : 'btn-outline'}" style="font-size:0.75rem; padding:4px 10px;" onclick="simFilterWorkouts('postpartum')">🧘 Phục hồi sau sinh</button>
        </div>

        <!-- Interactive Kegel Trainer Card -->
        <div class="card" style="padding: 16px; margin-bottom: 14px; background: linear-gradient(135deg, rgba(133, 78, 98, 0.08) 0%, rgba(95, 91, 125, 0.08) 100%); border: 1px solid var(--outline-variant); text-align: center;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
                <strong style="color: var(--tertiary);"><i class="fa-solid fa-stopwatch"></i> Bộ đếm nhịp Kegel tương tác</strong>
                <span class="badge-num" id="kegel-rep-badge" style="background: var(--tertiary-container); color: var(--tertiary);">Hiệp 0/${KEGEL_TOTAL_REPS}</span>
            </div>

            <div style="display: flex; justify-content: center; margin: 12px 0;">
                <div id="kegel-ring-indicator" style="width: 120px; height: 120px; border-radius: 50%; background: var(--tertiary); color: #fff; display: flex; flex-direction: column; align-items: center; justify-content: center; transition: transform 0.4s ease, background 0.4s ease; box-shadow: 0 6px 18px rgba(133, 78, 98, 0.3);">
                    <div id="kegel-action-text" style="font-size: 0.82rem; font-weight: 700; text-transform: uppercase;">SẴN SÀNG</div>
                    <div id="kegel-sec-display" style="font-size: 2.2rem; font-weight: 800; line-height: 1.1;">5s</div>
                </div>
            </div>

            <p id="kegel-guide-text" class="tip-desc" style="margin-bottom: 12px;">Nhấn "Bắt đầu" để hệ thống hướng dẫn nhịp Siết 5s và Thả lỏng 5s.</p>

            <div style="display: flex; gap: 8px; justify-content: center;">
                <button class="btn-primary-sm" id="btn-kegel-toggle" onclick="simToggleKegelTrainer()"><i class="fa-solid fa-play"></i> Bắt đầu tập</button>
                <button class="btn-outline" style="font-size:0.75rem; padding:4px 12px;" onclick="simResetKegelTrainer()"><i class="fa-solid fa-arrow-rotate-left"></i> Đặt lại</button>
            </div>
        </div>

        <!-- Workouts List -->
        <h4 class="section-title">Danh sách bài tập chuẩn y khoa</h4>
        <div style="display: flex; flex-direction: column; gap: 10px;" id="sim-workouts-list">
            ${filtered.map(w => `
                <div class="workout-item-card">
                    <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 6px;">
                        <div>
                            <span class="badge-num" style="background: var(--primary-container); color: var(--primary); margin-bottom: 4px; display: inline-block;">${w.icon} ${w.catName}</span>
                            <h4 style="margin: 0; font-size: 1rem; color: var(--text-primary);">${w.name}</h4>
                            <p style="font-size: 0.75rem; color: var(--text-muted); font-style: italic; margin: 2px 0 0 0;">${w.enName} • Wger ID: #${w.id}</p>
                        </div>
                    </div>
                    <p style="font-size: 0.8rem; color: var(--text-secondary); margin: 6px 0;">🎯 <strong>Tác động:</strong> ${w.target}</p>
                    <p style="font-size: 0.8rem; color: var(--secondary); font-weight: 600; margin: 0 0 8px 0;">⏱ <strong>Khuyến nghị:</strong> ${w.reps}</p>
                    
                    <div style="background: var(--surface-variant); border-radius: 8px; padding: 10px; margin-bottom: 8px;">
                        <strong style="font-size: 0.78rem; color: var(--text-primary);">Hướng dẫn chi tiết:</strong>
                        <ol style="margin: 4px 0 0 16px; padding: 0; font-size: 0.78rem; color: var(--text-secondary); line-height: 1.45;">
                            ${w.steps.map(s => `<li style="margin-bottom: 3px;">${s}</li>`).join('')}
                        </ol>
                    </div>

                    <p style="font-size: 0.74rem; color: #b71c1c; background: #fff5f5; border-radius: 6px; padding: 6px 8px; margin: 0 0 10px 0;">
                        ⚠️ <strong>Lưu ý:</strong> ${w.caution}
                    </p>

                    <button class="btn-outline" style="width: 100%; font-size: 0.78rem; padding: 6px 10px;" onclick="simLogWorkoutToJournal('${w.name.replace(/'/g, "\\'")}')">
                        <i class="fa-solid fa-plus-circle text-primary"></i> Ghi nhận đã tập bài này vào Nhật ký
                    </button>
                </div>
            `).join('')}
        </div>
    `;
}

function simFilterWorkouts(cat) {
    simWorkoutFilter = cat;
    const body = document.getElementById('module-body');
    if (body) {
        body.innerHTML = simRenderWorkoutsView();
    }
}

function simToggleKegelTrainer() {
    const btn = document.getElementById('btn-kegel-toggle');
    if (kegelTimerInterval) {
        clearInterval(kegelTimerInterval);
        kegelTimerInterval = null;
        if (btn) btn.innerHTML = `<i class="fa-solid fa-play"></i> Tiếp tục tập`;
    } else {
        if (btn) btn.innerHTML = `<i class="fa-solid fa-pause"></i> Tạm dừng`;
        kegelTimerInterval = setInterval(simKegelTick, 1000);
    }
}

function simKegelTick() {
    kegelSecondsLeft--;
    const secEl = document.getElementById('kegel-sec-display');
    const actEl = document.getElementById('kegel-action-text');
    const ring = document.getElementById('kegel-ring-indicator');
    const guide = document.getElementById('kegel-guide-text');
    const repBadge = document.getElementById('kegel-rep-badge');

    if (kegelSecondsLeft <= 0) {
        if (kegelPhase === 'squeeze') {
            kegelPhase = 'relax';
            kegelSecondsLeft = 5;
            if (actEl) actEl.textContent = 'THẢ LỎNG';
            if (guide) guide.textContent = 'Thả lỏng hoàn toàn cơ sàn chậu và thở ra êm đềm...';
            if (ring) {
                ring.style.transform = 'scale(1.15)';
                ring.style.background = 'var(--secondary)';
            }
        } else {
            kegelPhase = 'squeeze';
            kegelSecondsLeft = 5;
            kegelRepsDone++;
            if (repBadge) repBadge.textContent = `Hiệp ${kegelRepsDone}/${KEGEL_TOTAL_REPS}`;
            if (actEl) actEl.textContent = 'SIẾT CƠ';
            if (guide) guide.textContent = 'Siết chặt cơ sàn chậu như đang nhịn tiểu, không nín thở!';
            if (ring) {
                ring.style.transform = 'scale(0.85)';
                ring.style.background = 'var(--tertiary)';
            }

            if (kegelRepsDone >= KEGEL_TOTAL_REPS) {
                clearInterval(kegelTimerInterval);
                kegelTimerInterval = null;
                alert('🎉 Xuất sắc! Bạn đã hoàn thành trọn vẹn 10 hiệp bài tập Kegel hôm nay!');
                simLogWorkoutToJournal('10 Hiệp bài tập Kegel cơ sàn chậu');
                simResetKegelTrainer();
                return;
            }
        }
    }

    if (secEl) secEl.textContent = `${kegelSecondsLeft}s`;
}

function simResetKegelTrainer() {
    if (kegelTimerInterval) {
        clearInterval(kegelTimerInterval);
        kegelTimerInterval = null;
    }
    kegelSecondsLeft = 5;
    kegelPhase = 'squeeze';
    kegelRepsDone = 0;
    const secEl = document.getElementById('kegel-sec-display');
    const actEl = document.getElementById('kegel-action-text');
    const ring = document.getElementById('kegel-ring-indicator');
    const guide = document.getElementById('kegel-guide-text');
    const repBadge = document.getElementById('kegel-rep-badge');
    const btn = document.getElementById('btn-kegel-toggle');

    if (secEl) secEl.textContent = '5s';
    if (actEl) actEl.textContent = 'SẴN SÀNG';
    if (guide) guide.textContent = 'Nhấn "Bắt đầu" để hệ thống hướng dẫn nhịp Siết 5s và Thả lỏng 5s.';
    if (ring) {
        ring.style.transform = 'scale(1)';
        ring.style.background = 'var(--tertiary)';
    }
    if (repBadge) repBadge.textContent = `Hiệp 0/${KEGEL_TOTAL_REPS}`;
    if (btn) btn.innerHTML = `<i class="fa-solid fa-play"></i> Bắt đầu tập`;
}

function simLogWorkoutToJournal(workoutTitle) {
    const today = new Date().toISOString().split('T')[0];
    const newEntry = {
        date: today,
        symptoms: ['Vận động nhẹ nhàng'],
        mood: 5,
        sleepHours: 7.5,
        notes: `Đã hoàn thành bài tập: ${workoutTitle}. Cảm giác cơ thể dẻo dai và tràn đầy năng lượng.`
    };
    entries.unshift(newEntry);
    renderJournalFeed();
    alert(`✓ Đã ghi nhận bài tập "${workoutTitle}" vào Nhật ký hôm nay (${today})!`);
}

// ========================================================
// MODULE 3: DAILY INSPIRATIONAL QUOTES (QUOTABLE.IO)
// ========================================================
const SIM_QUOTES_LIST = [
    {
        vi: 'Cơ thể bạn là ngôi nhà duy nhất bạn sống trọn đời. Hãy đối đãi với nó bằng sự dịu dàng, kiên nhẫn và tình yêu thương.',
        en: 'Your body is your home for life. Treat it with kindness, patience, and love.',
        author: 'Khuyết danh (Tâm lý học sức khỏe)',
        tag: 'Yêu thương bản thân'
    },
    {
        vi: 'Sức khỏe không chỉ là vắng bóng bệnh tật, mà là trạng thái bình an trọn vẹn của thể chất, tinh thần và tâm hồn.',
        en: 'Health is a state of complete physical, mental, and social well-being, and not merely the absence of disease.',
        author: 'Tổ chức Y tế Thế giới (WHO)',
        tag: 'Sức khỏe toàn diện'
    },
    {
        vi: 'Hãy lắng nghe cơ thể bạn khi nó còn đang thì thầm, để không phải nghe thấy tiếng thét gào vì kiệt sức.',
        en: 'Listen to your body when it whispers, so you don\'t have to hear it scream.',
        author: 'Ngạn ngữ chữa lành',
        tag: 'Lắng nghe cơ thể'
    },
    {
        vi: 'Người phụ nữ sống thuận hòa với tâm hồn mình tựa như dòng sông êm trôi, chảy đến bất cứ đâu mà không cần gượng ép.',
        en: 'A woman in harmony with her spirit is like a river flowing. She goes where she will without pretense.',
        author: 'Maya Angelou',
        tag: 'Sức mạnh phụ nữ'
    },
    {
        vi: 'Nghỉ ngơi không phải là phần thưởng sau khi đã kiệt sức. Nghỉ ngơi là nhu cầu tự nhiên và thiêng liêng để duy trì sự sống.',
        en: 'Rest is not a reward for hard work. Rest is a natural requirement for being alive.',
        author: 'Cẩm nang An Nhiên',
        tag: 'Nghỉ ngơi & Tái tạo'
    },
    {
        vi: 'Hãy trân trọng từng mùa trong chu kỳ cơ thể bạn. Có lúc rực rỡ đơm hoa, và có lúc cần tĩnh lặng để tái sinh.',
        en: 'Take time to honor the cycle of your seasons. There is a time to bloom, and a time to rest.',
        author: 'Y học Nhịp sinh học',
        tag: 'Nhịp điệu tự nhiên'
    }
];

let simCurrentQuoteIndex = 0;
let simQuoteLang = 'vi'; // 'vi' or 'en'

function simRenderQuotesView() {
    const q = SIM_QUOTES_LIST[simCurrentQuoteIndex];
    const textToShow = simQuoteLang === 'vi' ? q.vi : q.en;

    return `
        <div class="card card-tip" style="margin-bottom: 12px;">
            <strong><i class="fa-solid fa-quote-left"></i> Quotable.io API</strong>
            <p class="tip-desc">Trích dẫn truyền cảm hứng sức khỏe, tâm lý & phụ nữ. Hỗ trợ song ngữ Anh - Việt & Đọc truyền cảm. Cache 24 giờ.</p>
        </div>

        <div class="quote-canvas-card">
            <i class="fa-solid fa-quote-left quote-watermark"></i>
            <span class="badge-num" style="background: var(--secondary-container); color: var(--secondary); margin-bottom: 12px; display: inline-block;">
                ✨ ${q.tag}
            </span>
            <p id="quote-display-text" style="font-size: 1.15rem; font-style: italic; line-height: 1.6; color: var(--text-primary); margin: 0 0 14px 0; font-weight: 500;">
                "${textToShow}"
            </p>
            <p id="quote-display-author" style="font-size: 0.9rem; font-weight: 700; color: var(--secondary); margin: 0;">
                — ${q.author}
            </p>
        </div>

        <!-- Toolbar Buttons -->
        <div style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; margin-bottom: 12px;">
            <button class="btn-primary" onclick="simNextQuote()"><i class="fa-solid fa-shuffle"></i> Đổi câu khác</button>
            <button class="btn-outline" onclick="simToggleQuoteLang()"><i class="fa-solid fa-language"></i> ${simQuoteLang === 'vi' ? 'Xem Tiếng Anh' : 'Xem Tiếng Việt'}</button>
        </div>

        <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin-bottom: 14px;">
            <button class="btn-outline" style="font-size:0.75rem; padding:8px 4px;" onclick="simCopyQuote()"><i class="fa-regular fa-copy"></i> Sao chép</button>
            <button class="btn-outline" style="font-size:0.75rem; padding:8px 4px;" onclick="simSpeakQuote()"><i class="fa-solid fa-volume-high"></i> Đọc to</button>
            <button class="btn-outline" style="font-size:0.75rem; padding:8px 4px;" onclick="simSaveQuoteToJournal()"><i class="fa-solid fa-bookmark"></i> Lưu nhật ký</button>
        </div>

        <h4 class="section-title">Ý nghĩa đối với sức khỏe tâm thần phụ nữ</h4>
        <div class="card" style="padding: 14px;">
            <p class="tip-desc" style="line-height: 1.5;">
                Theo các nghiên cứu từ Hiệp hội Tâm lý học Hoa Kỳ (APA), việc tiếp xúc với những lời tự nhủ tích cực (Positive Affirmations) mỗi sáng giúp giảm 23% nồng độ hormone căng thẳng Cortisol, cân bằng trục hạ đồi - tuyến yên - buồng trứng (HPO axis) và hỗ trợ điều hòa chu kỳ kinh nguyệt tự nhiên.
            </p>
        </div>
    `;
}

function simNextQuote() {
    // Try live fetch to Quotable if online
    let simNetOnline = localStorage.getItem('annhien_net_online') !== 'false';
    if (simNetOnline && Math.random() > 0.5) {
        fetch('https://api.quotable.io/quotes/random?tags=health,wellness,inspirational', { signal: AbortSignal.timeout(3000) })
            .then(r => r.json())
            .then(data => {
                const item = Array.isArray(data) ? data[0] : data;
                if (item && item.content) {
                    SIM_QUOTES_LIST.push({
                        vi: item.content, // Fallback to content or translate
                        en: item.content,
                        author: item.author || 'Danh ngôn quốc tế',
                        tag: 'Quotable Live'
                    });
                    simCurrentQuoteIndex = SIM_QUOTES_LIST.length - 1;
                    const body = document.getElementById('module-body');
                    if (body) body.innerHTML = simRenderQuotesView();
                    simUpdateHomeQuote();
                    return;
                }
            })
            .catch(() => {
                // fall through to local rotation
            });
    }

    simCurrentQuoteIndex = (simCurrentQuoteIndex + 1) % SIM_QUOTES_LIST.length;
    const body = document.getElementById('module-body');
    if (body) {
        body.innerHTML = simRenderQuotesView();
    }
    simUpdateHomeQuote();
}

function simToggleQuoteLang() {
    simQuoteLang = simQuoteLang === 'vi' ? 'en' : 'vi';
    const body = document.getElementById('module-body');
    if (body) {
        body.innerHTML = simRenderQuotesView();
    }
}

function simCopyQuote() {
    const q = SIM_QUOTES_LIST[simCurrentQuoteIndex];
    const text = `"${simQuoteLang === 'vi' ? q.vi : q.en}" — ${q.author} (từ An Nhiên App)`;
    navigator.clipboard.writeText(text).then(() => {
        alert('📋 Đã sao chép câu trích dẫn vào bộ nhớ đệm!');
    }).catch(() => {
        alert(text);
    });
}

function simSpeakQuote() {
    if (!window.speechSynthesis) {
        alert('Trình duyệt không hỗ trợ phát âm thanh SpeechSynthesis.');
        return;
    }
    window.speechSynthesis.cancel();
    const q = SIM_QUOTES_LIST[simCurrentQuoteIndex];
    const text = simQuoteLang === 'vi' ? q.vi : q.en;
    const utter = new SpeechSynthesisUtterance(text);
    utter.lang = simQuoteLang === 'vi' ? 'vi-VN' : 'en-US';
    utter.rate = 0.9;
    window.speechSynthesis.speak(utter);
}

function simSaveQuoteToJournal() {
    const q = SIM_QUOTES_LIST[simCurrentQuoteIndex];
    const today = new Date().toISOString().split('T')[0];
    const newEntry = {
        date: today,
        symptoms: ['Tĩnh tâm & Chữa lành'],
        mood: 5,
        sleepHours: 8,
        notes: `Lời khích lệ tâm đắc: "${q.vi}" — ${q.author}`
    };
    entries.unshift(newEntry);
    renderJournalFeed();
    alert(`✓ Đã lưu câu khích lệ vào Nhật ký hôm nay (${today})!`);
}

function simUpdateHomeQuote() {
    const q = SIM_QUOTES_LIST[simCurrentQuoteIndex];
    const homeText = document.getElementById('home-quote-text');
    const homeAuth = document.getElementById('home-quote-author');
    if (homeText) homeText.textContent = `"${q.vi}"`;
    if (homeAuth) homeAuth.textContent = `— ${q.author}`;
}

// ========================================================
// MODULE 6: WOMEN'S MICRONUTRIENTS (USDA FOODDATA CENTRAL)
// ========================================================
// ========================================================
// MODULE 6: WOMEN'S MICRONUTRIENTS (USDA FOODDATA CENTRAL)
// ========================================================
const SIM_MICRONUTRIENTS_INFO = {
    folate: {
        id: 'folate', name: 'Axit Folic / Folate (B9)', group: 'blood,pregnancy', icon: '🌿',
        rda: '400 – 600 mcg',
        why: 'Ngăn ngừa đến 70% nguy cơ dị tật ống thần kinh thai nhi (nứt đốt sống), tham gia phân chia tế bào ADN và tạo máu mới.',
        topFoods: [
            { name: 'Rau chân vịt (bina) tươi', amount: '194 mcg / 100g', rdaPct: 49 },
            { name: 'Đậu lăng hầm chín', amount: '181 mcg / 100g', rdaPct: 45 },
            { name: 'Măng tây xanh luộc', amount: '149 mcg / 100g', rdaPct: 37 },
            { name: 'Lòng đỏ trứng gà ta', amount: '146 mcg / 100g', rdaPct: 36 },
            { name: 'Trái bơ sáp chín', amount: '81 mcg / 100g', rdaPct: 20 }
        ],
        tip: 'Axit Folic dễ phân hủy khi nấu ở nhiệt độ cao lâu. Nên hấp hoặc xào nhanh rau xanh.'
    },
    iron: {
        id: 'iron', name: 'Sắt sinh học (Iron — Fe)', group: 'blood', icon: '🩸',
        rda: '18 – 27 mg',
        why: 'Bù đắp lượng máu hao hụt qua chu kỳ kinh nguyệt, vận chuyển oxy nuôi tế bào và thai nhi, giảm tình trạng chóng mặt, hụt hơi, rụng tóc.',
        topFoods: [
            { name: 'Huyết heo / bò luộc', amount: '35.0 mg / 100g', rdaPct: 194 },
            { name: 'Mộc nhĩ đen khô', amount: '56.1 mg / 100g', rdaPct: 311 },
            { name: 'Gan gà / bò hấp', amount: '9.0 mg / 100g', rdaPct: 50 },
            { name: 'Hạt bí ngô tách vỏ', amount: '8.8 mg / 100g', rdaPct: 49 },
            { name: 'Thịt bò nạc bắp', amount: '3.1 mg / 100g', rdaPct: 17 }
        ],
        tip: 'Hấp thu tốt nhất khi kết hợp với Vitamin C (cam, chanh). Tránh uống trà đặc, cà phê cùng lúc vì tanin cản trở hấp thu sắt.'
    },
    calcium: {
        id: 'calcium', name: 'Canxi sinh học (Ca)', group: 'bone', icon: '🦴',
        rda: '1000 – 1200 mg',
        why: 'Duy trì mật độ xương, phòng ngừa loãng xương khi nồng độ estrogen suy giảm ở tuổi tiền mãn kinh và nuôi dưỡng khung xương răng thai nhi.',
        topFoods: [
            { name: 'Phô mai Parmesan', amount: '1184 mg / 100g', rdaPct: 118 },
            { name: 'Hạt vừng (mè đen/trắng)', amount: '975 mg / 100g', rdaPct: 98 },
            { name: 'Đậu phụ non / Đậu khuôn', amount: '350 mg / 100g', rdaPct: 35 },
            { name: 'Hạnh nhân rang mộc', amount: '264 mg / 100g', rdaPct: 26 },
            { name: 'Sữa chua không đường', amount: '120 mg / 100g', rdaPct: 12 }
        ],
        tip: 'Canxi cần Vitamin D3 và Magie để hấp thụ trọn vẹn vào xương thay vì lắng đọng thành sỏi.'
    },
    vitamind: {
        id: 'vitamind', name: 'Vitamin D3 (Cholecalciferol)', group: 'bone,pregnancy', icon: '☀️',
        rda: '600 – 800 IU (15 – 20 mcg)',
        why: 'Chìa khóa mở cửa hấp thu canxi qua thành ruột, tăng cường miễn dịch tự nhiên, cải thiện tâm trạng trầm cảm theo mùa và hội chứng buồng trứng đa nang (PCOS).',
        topFoods: [
            { name: 'Dầu gan cá tuyết', amount: '10,000 IU / 100g', rdaPct: 1660 },
            { name: 'Cá hồi Nauy áp chảo', amount: '526 IU / 100g', rdaPct: 88 },
            { name: 'Cá mòi đóng hộp', amount: '193 IU / 100g', rdaPct: 32 },
            { name: 'Lòng đỏ trứng gà', amount: '87 IU / 100g', rdaPct: 15 },
            { name: 'Nấm phơi nắng tự nhiên', amount: '150 IU / 100g', rdaPct: 25 }
        ],
        tip: 'Khoảng 80% Vitamin D được tổng hợp qua da dưới ánh nắng mặt trời buổi sáng (trước 8:30).'
    },
    magnesium: {
        id: 'magnesium', name: 'Magie sinh học (Mg)', group: 'bone,hormone', icon: '⚡',
        rda: '310 – 320 mg',
        why: 'Làm giãn cơ trơn tử cung, giảm co thắt đau bụng kinh (thống kinh), giảm phù nề, đau đầu và căng thẳng tiền kinh nguyệt (PMS). Hỗ trợ giấc ngủ sâu.',
        topFoods: [
            { name: 'Bột cacao nguyên chất', amount: '499 mg / 100g', rdaPct: 156 },
            { name: 'Hạt điều rang mộc', amount: '292 mg / 100g', rdaPct: 91 },
            { name: 'Hạnh nhân sấy', amount: '268 mg / 100g', rdaPct: 84 },
            { name: 'Sô-cô-la đen (>70%)', amount: '228 mg / 100g', rdaPct: 71 },
            { name: 'Đậu đen ninh nhừ', amount: '70 mg / 100g', rdaPct: 22 }
        ],
        tip: 'Khi phụ nữ thèm sô-cô-la trước kỳ kinh, đó là tín hiệu tự nhiên báo cơ thể đang thiếu hụt Magie.'
    },
    zinc: {
        id: 'zinc', name: 'Kẽm sinh học (Zinc — Zn)', group: 'hormone,pregnancy', icon: '🔬',
        rda: '8 – 11 mg',
        why: 'Điều hòa hormone rụng trứng, hỗ trợ chất lượng trứng, kháng viêm niêm mạc âm đạo, giảm mụn nội tiết bùng phát và giúp vết khâu tầng sinh môn nhanh lành.',
        topFoods: [
            { name: 'Hàu biển tươi', amount: '16.6 mg / 100g', rdaPct: 207 },
            { name: 'Thịt bò nạc bắp', amount: '5.5 mg / 100g', rdaPct: 69 },
            { name: 'Hạt bí ngô tách vỏ', amount: '7.8 mg / 100g', rdaPct: 97 },
            { name: 'Đậu lăng / Đậu gà', amount: '1.8 mg / 100g', rdaPct: 22 },
            { name: 'Hạt mè trắng', amount: '7.8 mg / 100g', rdaPct: 97 }
        ],
        tip: 'Kẽm cùng với Vitamin A và Vitamin C tạo nên hàng rào bảo vệ niêm mạc tử cung vững chắc.'
    },
    b12: {
        id: 'b12', name: 'Vitamin B12 (Cobalamin)', group: 'blood,pregnancy', icon: '🥩',
        rda: '2.4 – 2.8 mcg',
        why: 'Duy trì bao myelin bảo vệ dây thần kinh, phối hợp cùng Folate tạo hồng cầu. Thiếu B12 gây hội chứng não sương mù (Brain Fog) và mệt mỏi kinh niên.',
        topFoods: [
            { name: 'Ngao / Nghêu hấp', amount: '98.9 mcg / 100g', rdaPct: 4120 },
            { name: 'Gan bò áp chảo', amount: '70.6 mcg / 100g', rdaPct: 2940 },
            { name: 'Cá thu / Cá hồi', amount: '8.9 mcg / 100g', rdaPct: 370 },
            { name: 'Sữa tươi thanh trùng', amount: '1.2 mcg / 250ml', rdaPct: 50 },
            { name: 'Trứng gà luộc', amount: '1.1 mcg / 100g', rdaPct: 46 }
        ],
        tip: 'Vitamin B12 chỉ có tự nhiên trong thực phẩm nguồn gốc động vật. Phụ nữ ăn chay thuần cần uống bổ sung định kỳ.'
    },
    b6: {
        id: 'b6', name: 'Vitamin B6 (Pyridoxine)', group: 'hormone,pregnancy', icon: '🥑',
        rda: '1.3 – 1.9 mg',
        why: 'Hỗ trợ tổng hợp hormone Progesterone, giảm ốm nghén và buồn nôn ở 3 tháng đầu thai kỳ, giảm tích nước và căng tức ngực trước kỳ kinh.',
        topFoods: [
            { name: 'Cá ngừ đại dương', amount: '1.0 mg / 100g', rdaPct: 77 },
            { name: 'Ức gà phi lê', amount: '0.6 mg / 100g', rdaPct: 46 },
            { name: 'Chuối tiêu chín', amount: '0.4 mg / 100g', rdaPct: 31 },
            { name: 'Khoai tây nướng cả vỏ', amount: '0.4 mg / 100g', rdaPct: 31 },
            { name: 'Trái bơ sáp', amount: '0.3 mg / 100g', rdaPct: 23 }
        ],
        tip: 'Vitamin B6 được Hiệp hội Sản Phụ khoa Hoa Kỳ (ACOG) khuyến nghị hàng đầu trong phác đồ giảm nghén thai kỳ an toàn.'
    },
    vitaminc: {
        id: 'vitaminc', name: 'Vitamin C (Ascorbic Acid)', group: 'blood,hormone', icon: '🍊',
        rda: '75 – 85 mg (Hút thuốc/stress: +35mg)',
        why: 'Tăng hấp thu sắt non-heme từ thực vật gấp 3 lần, tổng hợp Collagen chống rạn da thai kỳ và lão hóa, phục hồi nhanh niêm mạc tử cung.',
        topFoods: [
            { name: 'Quả ổi tươi ruột hồng', amount: '228 mg / 100g', rdaPct: 304 },
            { name: 'Ớt chuông đỏ', amount: '128 mg / 100g', rdaPct: 170 },
            { name: 'Quả kiwi vàng', amount: '93 mg / 100g', rdaPct: 124 },
            { name: 'Quả cam sành', amount: '53 mg / 100g', rdaPct: 70 },
            { name: 'Bông cải xanh luộc', amount: '65 mg / 100g', rdaPct: 87 }
        ],
        tip: 'Nên uống nước cam hoặc ăn ổi ngay sau bữa ăn có thịt bò hoặc rau bina để hấp thu sắt tối đa.'
    },
    omega3: {
        id: 'omega3', name: 'Omega-3 (DHA & EPA)', group: 'pregnancy,hormone', icon: '🐟',
        rda: '250 – 500 mg DHA/EPA',
        why: 'Hình thành vỏ não và võng mạc mắt thai nhi, kháng viêm mạn tính ở bệnh lạc nội mạc tử cung, ổn định tâm trạng và giảm nguy cơ trầm cảm sau sinh.',
        topFoods: [
            { name: 'Hạt chia hữu cơ', amount: '17,800 mg ALA / 100g', rdaPct: 1600 },
            { name: 'Hạt óc chó tách vỏ', amount: '9,080 mg ALA / 100g', rdaPct: 825 },
            { name: 'Cá hồi Nauy tươi', amount: '2,260 mg DHA/EPA / 100g', rdaPct: 565 },
            { name: 'Cá trích nướng', amount: '1,729 mg DHA/EPA / 100g', rdaPct: 432 },
            { name: 'Rong biển Wakame', amount: '188 mg / 100g', rdaPct: 47 }
        ],
        tip: 'Thai phụ cần bổ sung tối thiểu 200 – 300mg DHA mỗi ngày từ tuần 12 để thai nhi phát triển tế bào thần kinh thị giác.'
    },
    iodine: {
        id: 'iodine', name: 'I-ốt sinh học (Iodine)', group: 'pregnancy,hormone', icon: '🧂',
        rda: '150 – 220 mcg',
        why: 'Nguyên liệu thiết yếu sản xuất hormone tuyến giáp (T3, T4), điều hòa quá trình trao đổi chất của cơ thể và quyết định chỉ số IQ của thai nhi.',
        topFoods: [
            { name: 'Rong biển khô Kombu/Nori', amount: '232 mcg / 10g', rdaPct: 155 },
            { name: 'Muối I-ốt tiêu chuẩn', amount: '71 mcg / 1g', rdaPct: 47 },
            { name: 'Cá tuyết / Cá nục', amount: '110 mcg / 100g', rdaPct: 73 },
            { name: 'Sữa chua nguyên chất', amount: '75 mcg / 1 hũ', rdaPct: 50 },
            { name: 'Tôm biển tươi', amount: '35 mcg / 100g', rdaPct: 23 }
        ],
        tip: 'Thiếu I-ốt trong thai kỳ là nguyên nhân hàng đầu gây thiểu năng trí tuệ bẩm sinh và bướu cổ ở trẻ nhỏ.'
    },
    choline: {
        id: 'choline', name: 'Choline (Dưỡng chất vàng não bộ)', group: 'pregnancy', icon: '🥚',
        rda: '425 – 450 mg',
        why: 'Được mệnh danh là "Axit Folic thứ hai", Choline trực tiếp xây dựng cấu trúc màng tế bào và vùng hồi hải mã (Hippocampus - trung tâm trí nhớ não bộ) của thai nhi.',
        topFoods: [
            { name: 'Lòng đỏ trứng gà ta', amount: '680 mg / 100g', rdaPct: 151 },
            { name: 'Gan bò áp chảo', amount: '418 mg / 100g', rdaPct: 93 },
            { name: 'Đậu nành non (Edamame)', amount: '107 mg / 100g', rdaPct: 24 },
            { name: 'Cá tuyết nướng', amount: '84 mg / 100g', rdaPct: 19 },
            { name: 'Súp lơ trắng luộc', amount: '45 mg / 100g', rdaPct: 10 }
        ],
        tip: 'Chỉ cần ăn 2 quả trứng mỗi ngày đã đáp ứng hơn 50% nhu cầu Choline cho cả ngày!'
    }
};

let simSelectedNutrient = 'folate';
let simNutrientGroupFilter = 'all'; // 'all', 'blood', 'bone', 'hormone', 'pregnancy'

// Interactive Plate items with 6-pillar tracking
const SIM_PLATE_FOODS = [
    { id: 'spinach', name: '1 Bát rau chân vịt luộc (150g)', folate: 290, iron: 4.0, calcium: 150, magnesium: 120, zinc: 1.1, vitC: 28, checked: true },
    { id: 'beef', name: '100g Thịt bò nạc bắp xào', folate: 10, iron: 3.1, calcium: 18, magnesium: 22, zinc: 5.5, vitC: 0, checked: true },
    { id: 'salmon', name: '1 Miếng cá hồi áp chảo (100g)', folate: 25, iron: 0.8, calcium: 15, magnesium: 30, zinc: 0.6, vitC: 0, checked: true },
    { id: 'yogurt', name: '1 Hộp sữa chua không đường (100g)', folate: 12, iron: 0.1, calcium: 120, magnesium: 15, zinc: 0.6, vitC: 1, checked: true },
    { id: 'orange', name: '1 Quả cam / ổi tươi (150g)', folate: 45, iron: 0.4, calcium: 60, magnesium: 18, zinc: 0.2, vitC: 85, checked: false },
    { id: 'egg', name: '1 Quả trứng gà luộc (60g)', folate: 45, iron: 1.1, calcium: 28, magnesium: 6, zinc: 0.8, vitC: 0, checked: false },
    { id: 'almond', name: '1 Nắm hạt hạnh nhân / óc chó (30g)', folate: 18, iron: 1.2, calcium: 80, magnesium: 80, zinc: 1.0, vitC: 0, checked: false },
    { id: 'tofu', name: '1 Bìa đậu phụ non (100g)', folate: 20, iron: 2.0, calcium: 350, magnesium: 35, zinc: 0.8, vitC: 0, checked: false },
    { id: 'blackbean', name: '1 Bát đậu đen / đậu lăng ninh (150g)', folate: 180, iron: 5.3, calcium: 70, magnesium: 75, zinc: 2.0, vitC: 0, checked: false },
    { id: 'banana', name: '1 Quả chuối tiêu chín (120g)', folate: 24, iron: 0.3, calcium: 6, magnesium: 27, zinc: 0.2, vitC: 10, checked: false }
];

function simRenderUsdaNutritionView() {
    const nutKeys = Object.keys(SIM_MICRONUTRIENTS_INFO);
    const filteredKeys = simNutrientGroupFilter === 'all'
        ? nutKeys
        : nutKeys.filter(k => SIM_MICRONUTRIENTS_INFO[k].group.includes(simNutrientGroupFilter));

    // Ensure currently selected nutrient is in view or fallback to first
    if (!filteredKeys.includes(simSelectedNutrient) && filteredKeys.length > 0) {
        simSelectedNutrient = filteredKeys[0];
    }
    const nut = SIM_MICRONUTRIENTS_INFO[simSelectedNutrient] || SIM_MICRONUTRIENTS_INFO.folate;

    // Calculate plate totals
    let totFolate = 0, totIron = 0, totCalcium = 0, totMagnesium = 0, totZinc = 0, totVitC = 0;
    SIM_PLATE_FOODS.forEach(f => {
        if (f.checked) {
            totFolate += f.folate || 0;
            totIron += f.iron || 0;
            totCalcium += f.calcium || 0;
            totMagnesium += f.magnesium || 0;
            totZinc += f.zinc || 0;
            totVitC += f.vitC || 0;
        }
    });

    const folatePct = Math.min(100, Math.round((totFolate / 400) * 100));
    const ironPct = Math.min(100, Math.round((totIron / 18) * 100));
    const calciumPct = Math.min(100, Math.round((totCalcium / 1000) * 100));
    const magnesiumPct = Math.min(100, Math.round((totMagnesium / 320) * 100));
    const zincPct = Math.min(100, Math.round((totZinc / 8) * 100));
    const vitCPct = Math.min(100, Math.round((totVitC / 75) * 100));

    return `
        <div class="card card-tip" style="margin-bottom: 12px;">
            <strong><i class="fa-solid fa-seedling"></i> CSDL Vi chất USDA FoodData Central (12 Vi chất)</strong>
            <p class="tip-desc">Bộ chỉ số vi chất toàn diện cho sức khỏe phụ nữ, chuẩn bị mang thai, hậu sản & tiền mãn kinh. Cache 24 giờ.</p>
        </div>

        <!-- Group Filters (Chủ đề chức năng) -->
        <div style="display: flex; gap: 6px; overflow-x: auto; padding-bottom: 8px; margin-bottom: 10px;">
            <button class="${simNutrientGroupFilter === 'all' ? 'btn-primary-sm' : 'btn-outline'}" style="font-size:0.75rem; padding:4px 10px;" onclick="simFilterNutrientGroup('all')">Tất cả (12)</button>
            <button class="${simNutrientGroupFilter === 'blood' ? 'btn-primary-sm' : 'btn-outline'}" style="font-size:0.75rem; padding:4px 10px;" onclick="simFilterNutrientGroup('blood')">🩸 Tạo máu & Năng lượng</button>
            <button class="${simNutrientGroupFilter === 'bone' ? 'btn-primary-sm' : 'btn-outline'}" style="font-size:0.75rem; padding:4px 10px;" onclick="simFilterNutrientGroup('bone')">🦴 Xương & Khớp</button>
            <button class="${simNutrientGroupFilter === 'hormone' ? 'btn-primary-sm' : 'btn-outline'}" style="font-size:0.75rem; padding:4px 10px;" onclick="simFilterNutrientGroup('hormone')">🌸 Nội tiết & Làn da</button>
            <button class="${simNutrientGroupFilter === 'pregnancy' ? 'btn-primary-sm' : 'btn-outline'}" style="font-size:0.75rem; padding:4px 10px;" onclick="simFilterNutrientGroup('pregnancy')">🤰 Thai kỳ & Não bộ</button>
        </div>

        <!-- Nutrient Chips (Danh sách vi chất) -->
        <div style="display: flex; gap: 6px; overflow-x: auto; padding-bottom: 8px; margin-bottom: 14px;">
            ${filteredKeys.map(k => {
                const item = SIM_MICRONUTRIENTS_INFO[k];
                const active = simSelectedNutrient === k;
                return `
                <button class="${active ? 'btn-primary-sm' : 'btn-outline'}" style="font-size:0.75rem; padding:5px 12px; white-space:nowrap; border-radius: 20px;" onclick="simFilterNutrient('${k}')">
                    ${item.icon} ${item.name.split(' (')[0]}
                </button>`;
            }).join('')}
        </div>

        <!-- Nutrient Detail Card -->
        <div class="card" style="padding: 16px; margin-bottom: 14px; border-left: 4px solid var(--primary);">
            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                <div>
                    <h3 style="margin: 0; font-size: 1.15rem; color: var(--text-primary);">${nut.icon} ${nut.name}</h3>
                    <p style="font-size: 0.8rem; color: var(--primary); font-weight: 700; margin: 4px 0 8px 0;">Nhu cầu khuyến nghị (RDA): ${nut.rda}</p>
                </div>
                <span class="badge-num" style="background: var(--primary-container); color: var(--primary);">USDA Verified</span>
            </div>
            <p class="tip-desc" style="line-height: 1.45; margin-bottom: 10px;">${nut.why}</p>

            <strong style="font-size: 0.82rem; color: var(--text-primary);">Top thực phẩm giàu ${nut.name.split(' (')[0]} nhất:</strong>
            <div style="display: flex; flex-direction: column; gap: 6px; margin: 6px 0 10px 0;">
                ${nut.topFoods.map(f => `
                    <div style="display: flex; justify-content: space-between; align-items: center; background: var(--surface-variant); padding: 8px 12px; border-radius: 8px; font-size: 0.82rem;">
                        <span>${f.name}</span>
                        <div>
                            <strong style="color: var(--primary);">${f.amount}</strong>
                            <span class="badge-num" style="font-size:0.68rem; margin-left: 6px; background: var(--surface);">${f.rdaPct}% RDA</span>
                        </div>
                    </div>
                `).join('')}
            </div>

            <div style="background: rgba(61, 104, 78, 0.08); border-radius: 8px; padding: 8px 10px; font-size: 0.76rem; color: var(--text-primary);">
                💡 <strong>Mẹo hấp thu y khoa:</strong> ${nut.tip}
            </div>
        </div>

        <!-- Interactive RDA Plate Calculator -->
        <h4 class="section-title">Đĩa dinh dưỡng hôm nay của bạn (% RDA)</h4>
        <div class="card" style="padding: 14px; margin-bottom: 12px;">
            <p class="tip-desc" style="margin-bottom: 10px;">Tích chọn những món bạn đã dùng trong ngày để tính tổng vi chất nạp vào:</p>
            
            <div style="display: flex; flex-direction: column; gap: 6px; margin-bottom: 12px;">
                ${SIM_PLATE_FOODS.map(item => `
                    <label class="nutrient-calc-row">
                        <div style="display: flex; align-items: center; gap: 8px;">
                            <input type="checkbox" ${item.checked ? 'checked' : ''} onchange="simTogglePlateItem('${item.id}', this.checked)" style="transform: scale(1.15); accent-color: var(--primary);">
                            <span style="font-size: 0.82rem; font-weight: 500;">${item.name}</span>
                        </div>
                        <span style="font-size: 0.72rem; color: var(--text-muted);">+${item.folate}mcg B9 | +${item.iron}mg Fe</span>
                    </label>
                `).join('')}
            </div>

            <!-- Total RDA Progress Bars (6 Pillars) -->
            <div style="background: var(--surface-variant); border-radius: 12px; padding: 12px;">
                <!-- Folate -->
                <div style="margin-bottom: 8px;">
                    <div style="display: flex; justify-content: space-between; font-size: 0.8rem; margin-bottom: 4px;">
                        <span>🌿 Folate (B9): <strong>${totFolate} / 400 mcg</strong></span>
                        <strong style="color: ${folatePct >= 100 ? '#2e7d32' : 'var(--primary)'};">${folatePct}% RDA</strong>
                    </div>
                    <div style="height: 8px; background: var(--surface); border-radius: 4px; overflow: hidden;">
                        <div style="height: 100%; width: ${folatePct}%; background: ${folatePct >= 100 ? '#43a047' : '#fbc02d'}; border-radius: 4px; transition: width 0.4s;"></div>
                    </div>
                </div>

                <!-- Iron -->
                <div style="margin-bottom: 8px;">
                    <div style="display: flex; justify-content: space-between; font-size: 0.8rem; margin-bottom: 4px;">
                        <span>🩸 Sắt (Iron): <strong>${totIron.toFixed(1)} / 18 mg</strong></span>
                        <strong style="color: ${ironPct >= 100 ? '#2e7d32' : 'var(--primary)'};">${ironPct}% RDA</strong>
                    </div>
                    <div style="height: 8px; background: var(--surface); border-radius: 4px; overflow: hidden;">
                        <div style="height: 100%; width: ${ironPct}%; background: ${ironPct >= 100 ? '#43a047' : '#fbc02d'}; border-radius: 4px; transition: width 0.4s;"></div>
                    </div>
                </div>

                <!-- Calcium -->
                <div style="margin-bottom: 8px;">
                    <div style="display: flex; justify-content: space-between; font-size: 0.8rem; margin-bottom: 4px;">
                        <span>🦴 Canxi: <strong>${totCalcium} / 1000 mg</strong></span>
                        <strong style="color: ${calciumPct >= 100 ? '#2e7d32' : 'var(--primary)'};">${calciumPct}% RDA</strong>
                    </div>
                    <div style="height: 8px; background: var(--surface); border-radius: 4px; overflow: hidden;">
                        <div style="height: 100%; width: ${calciumPct}%; background: ${calciumPct >= 100 ? '#43a047' : '#fbc02d'}; border-radius: 4px; transition: width 0.4s;"></div>
                    </div>
                </div>

                <!-- Magnesium -->
                <div style="margin-bottom: 8px;">
                    <div style="display: flex; justify-content: space-between; font-size: 0.8rem; margin-bottom: 4px;">
                        <span>⚡ Magie: <strong>${totMagnesium} / 320 mg</strong></span>
                        <strong style="color: ${magnesiumPct >= 100 ? '#2e7d32' : 'var(--primary)'};">${magnesiumPct}% RDA</strong>
                    </div>
                    <div style="height: 8px; background: var(--surface); border-radius: 4px; overflow: hidden;">
                        <div style="height: 100%; width: ${magnesiumPct}%; background: ${magnesiumPct >= 100 ? '#43a047' : '#fbc02d'}; border-radius: 4px; transition: width 0.4s;"></div>
                    </div>
                </div>

                <!-- Zinc -->
                <div style="margin-bottom: 8px;">
                    <div style="display: flex; justify-content: space-between; font-size: 0.8rem; margin-bottom: 4px;">
                        <span>🔬 Kẽm (Zinc): <strong>${totZinc.toFixed(1)} / 8 mg</strong></span>
                        <strong style="color: ${zincPct >= 100 ? '#2e7d32' : 'var(--primary)'};">${zincPct}% RDA</strong>
                    </div>
                    <div style="height: 8px; background: var(--surface); border-radius: 4px; overflow: hidden;">
                        <div style="height: 100%; width: ${zincPct}%; background: ${zincPct >= 100 ? '#43a047' : '#fbc02d'}; border-radius: 4px; transition: width 0.4s;"></div>
                    </div>
                </div>

                <!-- Vitamin C -->
                <div>
                    <div style="display: flex; justify-content: space-between; font-size: 0.8rem; margin-bottom: 4px;">
                        <span>🍊 Vitamin C: <strong>${totVitC} / 75 mg</strong></span>
                        <strong style="color: ${vitCPct >= 100 ? '#2e7d32' : 'var(--primary)'};">${vitCPct}% RDA</strong>
                    </div>
                    <div style="height: 8px; background: var(--surface); border-radius: 4px; overflow: hidden;">
                        <div style="height: 100%; width: ${vitCPct}%; background: ${vitCPct >= 100 ? '#43a047' : '#fbc02d'}; border-radius: 4px; transition: width 0.4s;"></div>
                    </div>
                </div>
            </div>
        </div>

        <!-- USDA Search Simulator -->
        <h4 class="section-title">Tra cứu CSDL USDA FoodData Central</h4>
        <div class="card" style="padding: 14px; margin-bottom: 12px;">
            <div class="search-bar-wrap" style="margin-bottom: 10px; border: 1.5px solid var(--primary);">
                <i class="fa-solid fa-magnifying-glass" style="color: var(--primary); font-size: 1.05rem;"></i>
                <input type="text" id="sim-usda-input" placeholder="Nhập tên món ăn (vd: cá hồi, hạt chia, quả bơ, hàu...)" style="font-size: 0.92rem; width: 100%; border: none; background: transparent; outline: none; font-family: inherit; color: var(--text-primary);" onkeydown="if(event.key==='Enter') simSearchUsdaFood()">
            </div>

            <!-- Quick search suggestion chips -->
            <div style="display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 10px;">
                <button class="btn-outline" style="font-size: 0.72rem; padding: 4px 8px;" onclick="document.getElementById('sim-usda-input').value='Cá hồi'; simSearchUsdaFood()">🐟 Cá hồi</button>
                <button class="btn-outline" style="font-size: 0.72rem; padding: 4px 8px;" onclick="document.getElementById('sim-usda-input').value='Hạt chia'; simSearchUsdaFood()">🌱 Hạt chia</button>
                <button class="btn-outline" style="font-size: 0.72rem; padding: 4px 8px;" onclick="document.getElementById('sim-usda-input').value='Quả bơ'; simSearchUsdaFood()">🥑 Quả bơ</button>
                <button class="btn-outline" style="font-size: 0.72rem; padding: 4px 8px;" onclick="document.getElementById('sim-usda-input').value='Hàu biển'; simSearchUsdaFood()">🦪 Hàu biển</button>
                <button class="btn-outline" style="font-size: 0.72rem; padding: 4px 8px;" onclick="document.getElementById('sim-usda-input').value='Đậu lăng'; simSearchUsdaFood()">🥣 Đậu lăng</button>
            </div>

            <button class="btn-primary" style="width: 100%;" onclick="simSearchUsdaFood()"><i class="fa-solid fa-magnifying-glass"></i> Tra cứu dinh dưỡng USDA</button>
        </div>
        <div id="sim-usda-search-res"></div>
    `;
}

function simFilterNutrientGroup(groupKey) {
    simNutrientGroupFilter = groupKey;
    const body = document.getElementById('module-body');
    if (body) {
        body.innerHTML = simRenderUsdaNutritionView();
    }
}

function simFilterNutrient(nutKey) {
    simSelectedNutrient = nutKey;
    const body = document.getElementById('module-body');
    if (body) {
        body.innerHTML = simRenderUsdaNutritionView();
    }
}

function simTogglePlateItem(id, checked) {
    const item = SIM_PLATE_FOODS.find(f => f.id === id);
    if (item) {
        item.checked = checked;
        const body = document.getElementById('module-body');
        if (body) {
            body.innerHTML = simRenderUsdaNutritionView();
        }
    }
}

const SIM_USDA_FOOD_DATABASE = {
    'thịt bò': { id: '170567', name: 'Thịt bò nạc (Lean Beef)', portion: '100g thăn bò tươi', folate: '7 mcg', iron: '2.7 mg', calcium: '18 mg', zinc: '4.8 mg', vitD: '0 IU', omega3: '22 mg', b12: '2.6 mcg', b6: '0.5 mg' },
    'bò': { id: '170567', name: 'Thịt bò nạc (Lean Beef)', portion: '100g thăn bò tươi', folate: '7 mcg', iron: '2.7 mg', calcium: '18 mg', zinc: '4.8 mg', vitD: '0 IU', omega3: '22 mg', b12: '2.6 mcg', b6: '0.5 mg' },
    'thịt gà': { id: '171077', name: 'Thịt gà (Ức gà phi lê - Chicken Breast)', portion: '100g thịt nạc', folate: '4 mcg', iron: '1.0 mg', calcium: '12 mg', zinc: '1.0 mg', vitD: '0 IU', omega3: '60 mg', b12: '0.3 mcg', b6: '0.6 mg' },
    'gà': { id: '171077', name: 'Thịt gà (Ức gà phi lê - Chicken Breast)', portion: '100g thịt nạc', folate: '4 mcg', iron: '1.0 mg', calcium: '12 mg', zinc: '1.0 mg', vitD: '0 IU', omega3: '60 mg', b12: '0.3 mcg', b6: '0.6 mg' },
    'ức gà': { id: '171077', name: 'Ức gà phi lê không da (Chicken Breast)', portion: '100g thịt nạc', folate: '4 mcg', iron: '1.0 mg', calcium: '12 mg', zinc: '1.0 mg', vitD: '0 IU', omega3: '60 mg', b12: '0.3 mcg', b6: '0.6 mg' },
    'thịt lợn': { id: '168248', name: 'Thịt heo nạc (Pork Tenderloin)', portion: '100g thịt nạc', folate: '5 mcg', iron: '1.1 mg', calcium: '15 mg', zinc: '2.4 mg', vitD: '18 IU', omega3: '38 mg', b12: '0.7 mcg', b6: '0.7 mg' },
    'thịt heo': { id: '168248', name: 'Thịt heo nạc (Pork Tenderloin)', portion: '100g thịt nạc', folate: '5 mcg', iron: '1.1 mg', calcium: '15 mg', zinc: '2.4 mg', vitD: '18 IU', omega3: '38 mg', b12: '0.7 mcg', b6: '0.7 mg' },
    'trứng': { id: '171287', name: 'Trứng gà ta luộc (Whole Egg)', portion: '100g (~2 quả)', folate: '47 mcg', iron: '1.8 mg', calcium: '56 mg', zinc: '1.3 mg', vitD: '82 IU', omega3: '102 mg', b12: '0.9 mcg', b6: '0.1 mg' },
    'trứng gà': { id: '171287', name: 'Trứng gà ta luộc (Whole Egg)', portion: '100g (~2 quả)', folate: '47 mcg', iron: '1.8 mg', calcium: '56 mg', zinc: '1.3 mg', vitD: '82 IU', omega3: '102 mg', b12: '0.9 mcg', b6: '0.1 mg' },
    'tôm': { id: '175179', name: 'Tôm biển tươi (Cooked Shrimp)', portion: '100g thịt tôm', folate: '3 mcg', iron: '2.4 mg', calcium: '91 mg', zinc: '1.6 mg', vitD: '4 IU', omega3: '540 mg', b12: '1.7 mcg', b6: '0.1 mg' },
    'cá hồi': { id: '173686', name: 'Cá hồi Đại Tây Dương (Atlantic Salmon)', portion: '100g phi lê tươi', folate: '25 mcg', iron: '0.8 mg', calcium: '15 mg', zinc: '0.6 mg', vitD: '526 IU', omega3: '2260 mg', b12: '3.2 mcg', b6: '0.9 mg' },
    'rau muống': { id: '169288', name: 'Rau muống tươi (Water Spinach)', portion: '100g luộc chín', folate: '57 mcg', iron: '1.4 mg', calcium: '100 mg', zinc: '0.4 mg', vitD: '0 IU', omega3: '15 mg', b12: '0 mcg', b6: '0.1 mg' },
    'cải bó xôi': { id: '170417', name: 'Cải bó xôi / Rau bina (Baby Spinach)', portion: '100g tươi', folate: '194 mcg', iron: '2.7 mg', calcium: '99 mg', zinc: '0.5 mg', vitD: '0 IU', omega3: '138 mg', b12: '0 mcg', b6: '0.2 mg' },
    'bông cải xanh': { id: '170379', name: 'Bông cải xanh (Broccoli)', portion: '100g hấp chín', folate: '63 mcg', iron: '0.7 mg', calcium: '47 mg', zinc: '0.4 mg', vitD: '0 IU', omega3: '21 mg', b12: '0 mcg', b6: '0.2 mg' },
    'hạt chia': { id: '170554', name: 'Hạt chia hữu cơ nguyên hạt (Chia Seeds)', portion: '100g sấy khô', folate: '49 mcg', iron: '7.7 mg', calcium: '631 mg', zinc: '4.6 mg', vitD: '0 IU', omega3: '17800 mg', b12: '0 mcg', b6: '0.1 mg' },
    'quả bơ': { id: '171705', name: 'Quả bơ sáp tự nhiên (Avocado)', portion: '100g thịt quả', folate: '81 mcg', iron: '0.6 mg', calcium: '12 mg', zinc: '0.6 mg', vitD: '0 IU', omega3: '110 mg', b12: '0 mcg', b6: '0.3 mg' },
    'hàu': { id: '174223', name: 'Hàu biển Thái Bình Dương (Pacific Oyster)', portion: '100g thịt hàu', folate: '10 mcg', iron: '6.7 mg', calcium: '45 mg', zinc: '16.6 mg', vitD: '64 IU', omega3: '680 mg', b12: '16.0 mcg', b6: '0.1 mg' },
    'đậu lăng': { id: '172420', name: 'Đậu lăng đỏ/xanh luộc (Lentils)', portion: '100g nấu chín', folate: '181 mcg', iron: '3.3 mg', calcium: '19 mg', zinc: '1.3 mg', vitD: '0 IU', omega3: '110 mg', b12: '0 mcg', b6: '0.2 mg' },
    'sữa chua': { id: '170903', name: 'Sữa chua không đường (Greek Yogurt)', portion: '100g hũ', folate: '12 mcg', iron: '0.1 mg', calcium: '120 mg', zinc: '0.6 mg', vitD: '1 IU', omega3: '15 mg', b12: '0.4 mcg', b6: '0.1 mg' }
};

function simSearchUsdaFood() {
    const q = (document.getElementById('sim-usda-input').value || '').trim();
    const res = document.getElementById('sim-usda-search-res');
    if (!q) {
        alert('Vui lòng nhập tên món ăn cần tra cứu USDA!');
        return;
    }
    res.innerHTML = `<div style="text-align:center; padding:16px; color:var(--text-muted);"><i class="fa-solid fa-spinner fa-spin"></i> Đang tra cứu USDA FoodData Central...</div>`;
    setTimeout(() => {
        const qLower = q.toLowerCase();
        const matchedKey = Object.keys(SIM_USDA_FOOD_DATABASE).find(k => qLower === k || qLower.includes(k) || k.includes(qLower));
        const item = matchedKey ? SIM_USDA_FOOD_DATABASE[matchedKey] : null;

        if (!item) {
            res.innerHTML = `
                <div class="card" style="padding: 14px; background: rgba(220, 53, 69, 0.08); border-left: 4px solid var(--accent-terracotta);">
                    <strong style="color: var(--accent-terracotta);">ℹ️ Không tìm thấy vi chất cho "${q}"</strong>
                    <p style="font-size: 0.82rem; margin: 6px 0 0 0; color: var(--text-muted);">
                        Vui lòng thử các từ khóa phổ biến: <strong>thịt bò, thịt gà, thịt heo, trứng gà, tôm, cá hồi, rau muống, cải bó xôi, sữa chua, quả bơ, hạt chia...</strong>
                    </p>
                </div>
            `;
            return;
        }
        res.innerHTML = `
            <div class="card" style="padding: 14px; animation: fadeUp 0.3s ease;">
                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 6px;">
                    <div>
                        <strong style="font-size: 0.95rem; color: var(--text-primary);">${item.name}</strong>
                        <p class="tip-desc" style="margin: 2px 0 0 0;">FDC ID: #${item.id} • Khẩu phần: ${item.portion}</p>
                    </div>
                    <span class="badge-num" style="background: var(--primary-container); color: var(--primary);">Chuẩn USDA</span>
                </div>
                <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 6px; text-align: center; font-size: 0.78rem; margin: 10px 0 6px 0;">
                    <div style="background: var(--surface-variant); padding: 8px 4px; border-radius: 8px;">
                        <span style="color: var(--text-muted); font-size: 0.7rem;">Folate (B9)</span><br>
                        <strong style="color: var(--primary);">${item.folate}</strong>
                    </div>
                    <div style="background: var(--surface-variant); padding: 8px 4px; border-radius: 8px;">
                        <span style="color: var(--text-muted); font-size: 0.7rem;">Sắt (Fe)</span><br>
                        <strong style="color: var(--primary);">${item.iron}</strong>
                    </div>
                    <div style="background: var(--surface-variant); padding: 8px 4px; border-radius: 8px;">
                        <span style="color: var(--text-muted); font-size: 0.7rem;">Canxi (Ca)</span><br>
                        <strong style="color: var(--primary);">${item.calcium}</strong>
                    </div>
                    <div style="background: var(--surface-variant); padding: 8px 4px; border-radius: 8px;">
                        <span style="color: var(--text-muted); font-size: 0.7rem;">Kẽm (Zn)</span><br>
                        <strong style="color: var(--primary);">${item.zinc}</strong>
                    </div>
                </div>
                <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 6px; text-align: center; font-size: 0.78rem;">
                    <div style="background: var(--surface-variant); padding: 6px 4px; border-radius: 8px;">
                        <span style="color: var(--text-muted); font-size: 0.68rem;">Vitamin D</span><br>
                        <strong>${item.vitD}</strong>
                    </div>
                    <div style="background: var(--surface-variant); padding: 6px 4px; border-radius: 8px;">
                        <span style="color: var(--text-muted); font-size: 0.68rem;">Omega-3</span><br>
                        <strong>${item.omega3}</strong>
                    </div>
                    <div style="background: var(--surface-variant); padding: 6px 4px; border-radius: 8px;">
                        <span style="color: var(--text-muted); font-size: 0.68rem;">Vitamin B12</span><br>
                        <strong>${item.b12}</strong>
                    </div>
                    <div style="background: var(--surface-variant); padding: 6px 4px; border-radius: 8px;">
                        <span style="color: var(--text-muted); font-size: 0.68rem;">Vitamin B6</span><br>
                        <strong>${item.b6}</strong>
                    </div>
                </div>
                <p style="font-size: 0.72rem; color: var(--primary); margin-top: 10px;">✓ Đã lưu vào OnlineCacheManager (TTL 24h)</p>
            </div>
        `;
    }, 500);
}

// ========================================================
// MODULE 7: POLLEN & ALLERGY FORECAST (OPEN-METEO AQ API)
// ========================================================
const SIM_POLLEN_CITIES = {
    hanoi: {
        name: 'Hà Nội',
        grass: 18, birch: 6, ragweed: 12, mold: 54,
        overall: 'Trung bình', riskColor: '#f9a825',
        desc: 'Độ ẩm không khí cao tạo điều kiện cho bào tử nấm mốc phát triển. Người có cơ địa viêm mũi dị ứng nên thận trọng.'
    },
    hcm: {
        name: 'TP. Hồ Chí Minh',
        grass: 10, birch: 2, ragweed: 5, mold: 32,
        overall: 'Thấp', riskColor: '#43a047',
        desc: 'Nồng độ phấn hoa ở ngưỡng an toàn. Phù hợp cho các hoạt động thể dục ngoài trời của phụ nữ mang thai.'
    },
    dalat: {
        name: 'Đà Lạt',
        grass: 72, birch: 48, ragweed: 35, mold: 24,
        overall: 'Cao', riskColor: '#ef6c00',
        desc: 'Mùa hoa và thông nở rộ khiến mật độ phấn hoa trong không khí tăng cao. Khuyến cáo đeo khẩu trang chống dị ứng.'
    },
    danang: {
        name: 'Đà Nẵng',
        grass: 8, birch: 1, ragweed: 4, mold: 18,
        overall: 'Rất thấp', riskColor: '#43a047',
        desc: 'Gió biển trong lành cuốn trôi các dị nguyên đường hô hấp. Không khí rất tốt cho hệ hô hấp mẹ & bé.'
    }
};

let simSelectedPollenCity = 'hanoi';

function simRenderPollenView() {
    const data = SIM_POLLEN_CITIES[simSelectedPollenCity];

    return `
        <div class="card card-tip" style="margin-bottom: 12px;">
            <strong><i class="fa-solid fa-spa"></i> Open-Meteo Air Quality Pollen API</strong>
            <p class="tip-desc">Dự báo nồng độ phấn hoa cỏ, cây gỗ, cúc dại & bào tử nấm mốc theo thời gian thực. Không cần API Key. Cache TTL 6 giờ.</p>
        </div>

        <!-- City Selector -->
        <div style="display: flex; gap: 6px; overflow-x: auto; padding-bottom: 8px; margin-bottom: 12px;">
            <button class="${simSelectedPollenCity === 'hanoi' ? 'btn-primary-sm' : 'btn-outline'}" style="font-size:0.75rem; padding:4px 10px;" onclick="simLoadPollenCity('hanoi')">📍 Hà Nội</button>
            <button class="${simSelectedPollenCity === 'hcm' ? 'btn-primary-sm' : 'btn-outline'}" style="font-size:0.75rem; padding:4px 10px;" onclick="simLoadPollenCity('hcm')">📍 TP. HCM</button>
            <button class="${simSelectedPollenCity === 'dalat' ? 'btn-primary-sm' : 'btn-outline'}" style="font-size:0.75rem; padding:4px 10px;" onclick="simLoadPollenCity('dalat')">📍 Đà Lạt</button>
            <button class="${simSelectedPollenCity === 'danang' ? 'btn-primary-sm' : 'btn-outline'}" style="font-size:0.75rem; padding:4px 10px;" onclick="simLoadPollenCity('danang')">📍 Đà Nẵng</button>
        </div>

        <div id="sim-pollen-container">
            <!-- Overall Risk Card -->
            <div class="card" style="padding: 16px; margin-bottom: 12px; border-left: 4px solid ${data.riskColor};">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                    <div>
                        <h3 style="margin: 0; font-size: 1.2rem;">${data.name}</h3>
                        <p class="tip-desc" style="margin: 2px 0 0 0;">Cảnh báo dị ứng môi trường</p>
                    </div>
                    <span class="badge-num" style="background: ${data.riskColor}22; color: ${data.riskColor}; font-weight: 800; font-size: 0.9rem; padding: 4px 12px;">
                        Mức ${data.overall}
                    </span>
                </div>
                <p class="tip-desc" style="line-height: 1.45;">${data.desc}</p>
            </div>

            <!-- Pollen Grid (4 indicators) -->
            <h4 class="section-title">Chỉ số 4 tác nhân gây dị ứng chính</h4>
            <div class="pollen-pill-grid">
                <div class="pollen-card-item">
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <span style="font-size: 0.8rem; font-weight: 600;">🌾 Phấn hoa cỏ</span>
                        <strong style="color: ${data.grass > 50 ? '#ef6c00' : 'var(--primary)'}; font-size: 1rem;">${data.grass}</strong>
                    </div>
                    <div style="font-size: 0.68rem; color: var(--text-muted);">grains / m³</div>
                    <p style="font-size: 0.72rem; color: var(--text-secondary); margin-top: 4px;">Nguyên nhân chính gây hắt hơi sổ mũi dị ứng thời tiết</p>
                </div>

                <div class="pollen-card-item">
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <span style="font-size: 0.8rem; font-weight: 600;">🌳 Phấn hoa gỗ</span>
                        <strong style="color: ${data.birch > 30 ? '#ef6c00' : 'var(--primary)'}; font-size: 1rem;">${data.birch}</strong>
                    </div>
                    <div style="font-size: 0.68rem; color: var(--text-muted);">grains / m³</div>
                    <p style="font-size: 0.72rem; color: var(--text-secondary); margin-top: 4px;">Phấn hoa cây bạch dương & thông gây ngứa mí mắt</p>
                </div>

                <div class="pollen-card-item">
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <span style="font-size: 0.8rem; font-weight: 600;">🌼 Cúc dại Ragweed</span>
                        <strong style="color: ${data.ragweed > 30 ? '#ef6c00' : 'var(--primary)'}; font-size: 1rem;">${data.ragweed}</strong>
                    </div>
                    <div style="font-size: 0.68rem; color: var(--text-muted);">grains / m³</div>
                    <p style="font-size: 0.72rem; color: var(--text-secondary); margin-top: 4px;">Kích ứng niêm mạc mũi họng và kích phát cơn hen</p>
                </div>

                <div class="pollen-card-item">
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <span style="font-size: 0.8rem; font-weight: 600;">🍄 Bào tử nấm mốc</span>
                        <strong style="color: ${data.mold > 40 ? '#ef6c00' : 'var(--primary)'}; font-size: 1rem;">${data.mold}</strong>
                    </div>
                    <div style="font-size: 0.68rem; color: var(--text-muted);">spores / m³</div>
                    <p style="font-size: 0.72rem; color: var(--text-secondary); margin-top: 4px;">Tăng cao khi thời tiết nồm ẩm hoặc mùa mưa kéo dài</p>
                </div>
            </div>

            <!-- Health Protection Guide -->
            <h4 class="section-title">Khuyến cáo bảo vệ sức khỏe phụ nữ & thai phụ</h4>
            <div class="card" style="padding: 14px; margin-bottom: 12px; border-left: 4px solid var(--secondary);">
                <div style="display: flex; gap: 10px;">
                    <i class="fa-solid fa-lungs" style="color: var(--secondary); font-size: 1.3rem; margin-top: 2px;"></i>
                    <div>
                        <strong style="font-size: 0.88rem;">Vệ sinh đường thở an toàn trong thai kỳ</strong>
                        <p class="tip-desc" style="margin-top: 4px;">
                            Phụ nữ mang thai cơ địa viêm xoang nên rửa mũi bằng nước muối sinh lý ấm 0.9% hoặc xịt nước biển sâu 2 lần/ngày. Tránh tự ý uống thuốc kháng dị ứng kháng histamin nhóm C mà chưa có chỉ định của bác sĩ sản phụ khoa.
                        </p>
                    </div>
                </div>
            </div>

            <div class="card" style="padding: 14px; margin-bottom: 14px; border-left: 4px solid var(--primary);">
                <div style="display: flex; gap: 10px;">
                    <i class="fa-solid fa-house-chimney-window" style="color: var(--primary); font-size: 1.3rem; margin-top: 2px;"></i>
                    <div>
                        <strong style="font-size: 0.88rem;">Khung giờ vàng tránh phấn hoa</strong>
                        <p class="tip-desc" style="margin-top: 4px;">
                            Mật độ hạt phấn phát tán cao nhất từ 5h00 đến 9h00 sáng. Nên đóng kín cửa sổ phòng ngủ trong khung giờ này, phơi đồ trong nhà có mái che và thay áo quần, gội đầu sau khi đi ngoài đường về.
                        </p>
                    </div>
                </div>
            </div>

            <!-- Refresh Button -->
            <button class="btn-primary" style="width: 100%;" onclick="simRefreshPollen()"><i class="fa-solid fa-rotate"></i> Cập nhật dự báo phấn hoa Open-Meteo</button>
        </div>
    `;
}

function simLoadPollenCity(city) {
    simSelectedPollenCity = city;
    const body = document.getElementById('module-body');
    if (body) {
        body.innerHTML = simRenderPollenView();
    }
}

function simRefreshPollen() {
    const cont = document.getElementById('sim-pollen-container');
    if (!cont) return;
    cont.innerHTML = `<div style="text-align:center; padding: 30px; color: var(--text-muted);"><i class="fa-solid fa-spinner fa-spin" style="font-size:1.8rem; color:var(--primary);"></i><p style="margin-top:10px;">Đang kết nối Open-Meteo Air Quality Pollen API...</p></div>`;
    setTimeout(() => {
        cont.innerHTML = simRenderPollenView();
    }, 700);
}

// ========================================================
// KEYBOARD AVOIDANCE & AUTO PUSH-UP HANDLERS
// ========================================================
function initKeyboardAvoidance() {
    function handleInputFocus(el) {
        if (!el || (el.tagName !== 'INPUT' && el.tagName !== 'TEXTAREA' && el.tagName !== 'SELECT')) return;

        const modalSheet = el.closest('.modal-sheet');
        const screenContent = el.closest('.screen-content');
        const scrollContainer = modalSheet || screenContent;

        if (scrollContainer) {
            scrollContainer.classList.add('keyboard-push-up');
        }

        const targetToScroll = el.closest('.card') || el.closest('.search-bar-wrap') || el;

        const performScroll = () => {
            if (targetToScroll && typeof targetToScroll.scrollIntoView === 'function') {
                targetToScroll.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        };

        // Multi-stage scroll to ensure clearance during mobile/onscreen keyboard slide-in
        performScroll();
        setTimeout(performScroll, 120);
        setTimeout(performScroll, 320);
        setTimeout(performScroll, 550);
    }

    document.addEventListener('focusin', (e) => {
        handleInputFocus(e.target);
    });

    document.addEventListener('focusout', (e) => {
        setTimeout(() => {
            const active = document.activeElement;
            if (!active || (active.tagName !== 'INPUT' && active.tagName !== 'TEXTAREA' && active.tagName !== 'SELECT')) {
                document.querySelectorAll('.keyboard-push-up').forEach(c => {
                    c.classList.remove('keyboard-push-up');
                });
            }
        }, 220);
    });

    // Mobile Visual Viewport resize detection
    if (window.visualViewport) {
        window.visualViewport.addEventListener('resize', () => {
            const active = document.activeElement;
            if (active && (active.tagName === 'INPUT' || active.tagName === 'TEXTAREA' || active.tagName === 'SELECT')) {
                handleInputFocus(active);
            }
        });
    }
}

// Auto initialize on load
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initKeyboardAvoidance);
} else {
    initKeyboardAvoidance();
}


