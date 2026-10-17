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
            body.innerHTML = `
                <div class="tabs-sub" style="display: flex; gap: 8px; margin-bottom: 12px;">
                    <button class="btn-primary-sm" style="flex: 1;" onclick="showMedSubTab('visits')">Lượt khám</button>
                    <button class="btn-outline" style="flex: 1;" onclick="showMedSubTab('meds')">Thuốc & Vi chất</button>
                </div>

                <div id="subtab-visits">
                    <h4 class="section-title">Thêm lượt khám mới</h4>
                    <div class="card">
                        <input type="text" placeholder="Cơ sở y tế (Bệnh viện / Phòng khám)" class="text-area" style="height: 44px; margin-bottom: 8px;" id="med-fac">
                        <input type="text" placeholder="Bác sĩ phụ trách" class="text-area" style="height: 44px; margin-bottom: 8px;" id="med-doc">
                        <textarea placeholder="Câu hỏi muốn hỏi bác sĩ..." class="text-area" style="height: 60px; margin-bottom: 8px;" id="med-ques"></textarea>
                        <textarea placeholder="Kết luận sau khám..." class="text-area" style="height: 60px; margin-bottom: 8px;" id="med-notes"></textarea>
                        <button class="btn-primary" onclick="saveMedVisit()">Lưu hồ sơ khám</button>
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
            `;
            break;

        case 'foodsearch':
            title.textContent = "Dinh dưỡng thực phẩm (Open Food Facts)";
            body.innerHTML = `
                <div class="card card-tip" style="margin-bottom: 12px;">
                    <strong><i class="fa-solid fa-cloud"></i> Nguồn mở Open Food Facts</strong>
                    <p class="tip-desc">Tra cứu thành phần dinh dưỡng, năng lượng (Kcal) và vi chất của hàng triệu sản phẩm.</p>
                </div>
                <div class="search-bar-wrap" style="margin-bottom: 12px;">
                    <i class="fa-solid fa-magnifying-glass"></i>
                    <input type="text" id="sim-food-query" placeholder="Nhập tên thực phẩm (vd: sữa chua, yến mạch...)" value="Yến mạch nguyên cám" style="border: none; background: transparent; width: 100%; outline: none;">
                </div>
                <div style="display: flex; gap: 8px; margin-bottom: 14px;">
                    <button class="btn-primary" style="flex: 1;" onclick="simSearchFood()"><i class="fa-solid fa-magnifying-glass"></i> Tra cứu</button>
                    <button class="btn-outline" onclick="simSampleBarcode()"><i class="fa-solid fa-barcode"></i> Mã vạch mẫu</button>
                </div>
                <div id="sim-food-results" style="display: flex; flex-direction: column; gap: 10px;">
                    <div class="card" style="padding: 14px;">
                        <div class="card-row">
                            <strong>Yến mạch cán dẹt Úc (Rolled Oats)</strong>
                            <span class="badge-num" style="background: var(--primary-container); color: var(--primary);">379 Kcal / 100g</span>
                        </div>
                        <p class="tip-desc">Thương hiệu: Quaker Oats • Quy cách: 500g</p>
                        <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 6px; margin-top: 10px; font-size: 0.8rem; text-align: center;">
                            <div style="background: var(--surface-variant); padding: 6px; border-radius: 8px;">Đạm: <strong>13.2g</strong></div>
                            <div style="background: var(--surface-variant); padding: 6px; border-radius: 8px;">Carb: <strong>67.7g</strong></div>
                            <div style="background: var(--surface-variant); padding: 6px; border-radius: 8px;">Xơ: <strong>10.1g</strong></div>
                        </div>
                    </div>
                </div>
            `;
            break;

        case 'healthtopics':
            title.textContent = "Đề tài sức khỏe phụ nữ (MyHealthfinder)";
            body.innerHTML = `
                <div class="card card-tip" style="margin-bottom: 12px;">
                    <strong><i class="fa-solid fa-user-doctor"></i> MyHealthfinder v4 API</strong>
                    <p class="tip-desc">Dữ liệu y tế dự phòng từ Bộ Y tế & Dịch vụ Nhân sinh Hoa Kỳ (HHS).</p>
                </div>
                <div style="display: flex; flex-direction: column; gap: 10px;">
                    <div class="card" style="padding: 14px;">
                        <span class="badge-num" style="background: var(--rose-light); color: var(--rose); margin-bottom: 6px; display: inline-block;">Sàng lọc phụ khoa</span>
                        <h4 style="margin: 0 0 6px 0;">Tầm soát ung thư cổ tử cung định kỳ</h4>
                        <p class="tip-desc">Khuyến nghị xét nghiệm Pap smear mỗi 3 năm cho phụ nữ từ 21-29 tuổi, và xét nghiệm phối hợp HPV mỗi 5 năm từ 30-65 tuổi.</p>
                    </div>
                    <div class="card" style="padding: 14px;">
                        <span class="badge-num" style="background: var(--lavender-light); color: var(--lavender); margin-bottom: 6px; display: inline-block;">Thai kỳ & Dinh dưỡng</span>
                        <h4 style="margin: 0 0 6px 0;">Bổ sung Axit Folic trước và trong thai kỳ</h4>
                        <p class="tip-desc">Phụ nữ dự định mang thai nên bổ sung ít nhất 400 mcg axit folic mỗi ngày để phòng ngừa dị tật ống thần kinh ở thai nhi.</p>
                    </div>
                    <div class="card" style="padding: 14px;">
                        <span class="badge-num" style="background: var(--terracotta-light); color: var(--terracotta); margin-bottom: 6px; display: inline-block;">Hậu sản</span>
                        <h4 style="margin: 0 0 6px 0;">Chăm sóc sức khỏe tinh thần sau sinh</h4>
                        <p class="tip-desc">Nhận diện sớm các dấu hiệu trầm cảm sau sinh (Baby Blues kéo dài trên 2 tuần) và trao đổi ngay với chuyên gia tâm lý hoặc bác sĩ.</p>
                    </div>
                </div>
            `;
            break;

        case 'airquality':
            title.textContent = "Chất lượng không khí (OpenAQ v3)";
            body.innerHTML = `
                <div class="card card-tip" style="margin-bottom: 12px;">
                    <strong><i class="fa-solid fa-wind"></i> Trạm quan trắc OpenAQ v3</strong>
                    <p class="tip-desc">Dữ liệu quan trắc hạt bụi mịn PM2.5, PM10 theo thời gian thực.</p>
                </div>
                <div class="card" style="padding: 14px; margin-bottom: 12px;">
                    <div class="card-row">
                        <div>
                            <strong>Tọa độ tìm kiếm:</strong>
                            <p class="tip-desc" id="sim-aq-coords">21.0278° N, 105.8342° E (Hà Nội)</p>
                        </div>
                        <button class="btn-primary-sm" onclick="simRefreshAirQuality()"><i class="fa-solid fa-rotate"></i> Cập nhật</button>
                    </div>
                </div>
                <div id="sim-aq-station" class="card" style="padding: 14px;">
                    <div class="card-row">
                        <strong>Trạm ĐSQ Hoa Kỳ — Hà Nội (ID: 3274)</strong>
                        <span class="badge-num" style="background: #FFF3CD; color: #856404;">AQI: 85 (Trung bình)</span>
                    </div>
                    <p class="tip-desc">Quốc gia: VN • Tọa độ: 21.02, 105.81</p>
                    <div style="margin-top: 10px; display: flex; justify-content: space-between; font-size: 0.85rem;">
                        <span>Bụi mịn PM2.5: <strong>28.4 µg/m³</strong></span>
                        <span style="color: var(--text-muted);">Cập nhật: 15 phút trước</span>
                    </div>
                    <p style="font-size: 0.75rem; color: var(--primary); margin-top: 8px;">✓ Dữ liệu lưu trong bộ nhớ tạm (Cache 30 phút)</p>
                </div>
            `;
            break;

        case 'onlineutilities':
            title.textContent = "Quản lý tiện ích trực tuyến";
            let simNetOnline = localStorage.getItem('annhien_net_online') !== 'false';
            body.innerHTML = `
                <div class="card" style="display: flex; justify-content: space-between; align-items: center; padding: 14px; margin-bottom: 14px;">
                    <div>
                        <strong>Cho phép kết nối Internet</strong>
                        <p class="tip-desc">Bật/tắt các tính năng tra cứu ngoài (Offline-First)</p>
                    </div>
                    <input type="checkbox" id="sim-net-toggle" ${simNetOnline ? 'checked' : ''} onchange="simToggleNetwork(this.checked)" style="transform: scale(1.3);">
                </div>
                <h4 class="section-title">Trạng thái nhà cung cấp & Hạn mức</h4>
                <div style="display: flex; flex-direction: column; gap: 8px;">
                    <div class="card" style="padding: 12px; display: flex; justify-content: space-between; align-items: center;">
                        <div>
                            <strong>Open Food Facts</strong>
                            <p class="tip-desc">User-Agent: AnNhien/1.0 • Miễn phí không giới hạn</p>
                        </div>
                        <span class="badge-num" style="background: var(--sage-light); color: var(--sage);">Sẵn sàng</span>
                    </div>
                    <div class="card" style="padding: 12px; display: flex; justify-content: space-between; align-items: center;">
                        <div>
                            <strong>MyHealthfinder v4</strong>
                            <p class="tip-desc">HHS USA API • Cache 72 giờ</p>
                        </div>
                        <span class="badge-num" style="background: var(--sage-light); color: var(--sage);">Sẵn sàng</span>
                    </div>
                    <div class="card" style="padding: 12px; display: flex; justify-content: space-between; align-items: center;">
                        <div>
                            <strong>OpenAQ v3</strong>
                            <p class="tip-desc">Header: X-API-Key • Cache 30 phút</p>
                        </div>
                        <span class="badge-num" style="background: var(--sage-light); color: var(--sage);">Sẵn sàng</span>
                    </div>
                </div>
                <div class="card" style="margin-top: 14px;">
                    <div class="card-row">
                        <div>
                            <strong>Bộ nhớ đệm (Cache)</strong>
                            <p class="tip-desc">Dung lượng tạm: ~48 KB (4 mục)</p>
                        </div>
                        <button class="btn-outline" onclick="alert('Đã dọn dẹp toàn bộ bộ nhớ tạm (OnlineCacheManager)!')">Xóa cache</button>
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

function saveMedVisit() {
    const fac = document.getElementById('med-fac').value;
    alert(`Đã lưu hồ sơ khám tại "${fac || 'Cơ sở y tế'}" vào bộ nhớ máy!`);
    closeModuleModal();
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
function simToggleNetwork(enabled) {
    localStorage.setItem('annhien_net_online', enabled ? 'true' : 'false');
    alert(enabled ? "Đã bật kết nối Internet cho các tiện ích tra cứu trực tuyến." : "Đã chuyển về chế độ 100% Ngoại tuyến (Offline-First).");
}

function simSearchFood() {
    const q = document.getElementById('sim-food-query').value.trim();
    const res = document.getElementById('sim-food-results');
    if (!q) {
        alert("Vui lòng nhập tên thực phẩm cần tra cứu!");
        return;
    }
    res.innerHTML = `
        <div class="card" style="padding: 14px;">
            <div class="card-row">
                <strong>${q}</strong>
                <span class="badge-num" style="background: var(--primary-container); color: var(--primary);">245 Kcal / 100g</span>
            </div>
            <p class="tip-desc">Dữ liệu nguồn: Open Food Facts API (Trực tuyến)</p>
            <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 6px; margin-top: 10px; font-size: 0.8rem; text-align: center;">
                <div style="background: var(--surface-variant); padding: 6px; border-radius: 8px;">Đạm: <strong>8.5g</strong></div>
                <div style="background: var(--surface-variant); padding: 6px; border-radius: 8px;">Carb: <strong>42.0g</strong></div>
                <div style="background: var(--surface-variant); padding: 6px; border-radius: 8px;">Chất béo: <strong>4.2g</strong></div>
            </div>
            <p style="font-size: 0.75rem; color: var(--primary); margin-top: 8px;">✓ Đã lưu vào bộ nhớ tạm để xem ngoại tuyến</p>
        </div>
    `;
}

function simSampleBarcode() {
    const res = document.getElementById('sim-food-results');
    res.innerHTML = `
        <div class="card" style="padding: 14px;">
            <div class="card-row">
                <strong>Sữa chua không đường Vinamilk (8934673100345)</strong>
                <span class="badge-num" style="background: var(--primary-container); color: var(--primary);">61 Kcal / 100g</span>
            </div>
            <p class="tip-desc">Thương hiệu: Vinamilk • Quy cách: Hộp 100g</p>
            <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 6px; margin-top: 10px; font-size: 0.8rem; text-align: center;">
                <div style="background: var(--surface-variant); padding: 6px; border-radius: 8px;">Canxi: <strong>110mg</strong></div>
                <div style="background: var(--surface-variant); padding: 6px; border-radius: 8px;">Đạm: <strong>3.7g</strong></div>
                <div style="background: var(--surface-variant); padding: 6px; border-radius: 8px;">Đường: <strong>4.5g</strong></div>
            </div>
            <p style="font-size: 0.75rem; color: var(--primary); margin-top: 8px;">✓ Quét mã vạch thành công từ Open Food Facts</p>
        </div>
    `;
}

function simRefreshAirQuality() {
    const el = document.getElementById('sim-aq-station');
    if (el) {
        el.innerHTML = `
            <div class="card-row">
                <strong>Trạm Láng — Hà Nội (ID: 4108)</strong>
                <span class="badge-num" style="background: #D4EDDA; color: #155724;">AQI: 42 (Tốt)</span>
            </div>
            <p class="tip-desc">Quốc gia: VN • Tọa độ: 21.018, 105.805</p>
            <div style="margin-top: 10px; display: flex; justify-content: space-between; font-size: 0.85rem;">
                <span>Bụi mịn PM2.5: <strong>10.2 µg/m³</strong></span>
                <span style="color: var(--text-muted);">Vừa cập nhật xong</span>
            </div>
            <p style="font-size: 0.75rem; color: var(--primary); margin-top: 8px;">✓ Kết nối OpenAQ v3 thành công</p>
        `;
    }
}
