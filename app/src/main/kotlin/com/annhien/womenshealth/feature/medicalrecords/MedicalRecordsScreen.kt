package com.annhien.womenshealth.feature.medicalrecords

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.annhien.womenshealth.core.database.entity.MedicalDocumentEntity
import com.annhien.womenshealth.core.database.entity.MedicalVisitEntity
import com.annhien.womenshealth.core.database.entity.MedicationScheduleEntity
import com.annhien.womenshealth.domain.repository.MedicalRecordsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class MedicalRecordsUiState(
    val visits: List<MedicalVisitEntity> = emptyList(),
    val medications: List<MedicationScheduleEntity> = emptyList(),
    val documents: Map<String, List<MedicalDocumentEntity>> = emptyMap(),
    val viewingPhotoPath: String? = null,
    val selectedTab: Int = 0, // 0: Khám bệnh, 1: Thuốc & Vi chất
    val showAddDialog: Boolean = false,
    val showAddMedicationDialog: Boolean = false
)

@HiltViewModel
class MedicalRecordsViewModel @Inject constructor(
    private val medicalRecordsRepository: MedicalRecordsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicalRecordsUiState())
    val uiState: StateFlow<MedicalRecordsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            medicalRecordsRepository.observeVisits().collect { list ->
                _uiState.update { it.copy(visits = list) }
            }
        }
        viewModelScope.launch {
            medicalRecordsRepository.observeMedicationSchedules().collect { list ->
                _uiState.update { it.copy(medications = list) }
            }
        }
        viewModelScope.launch {
            medicalRecordsRepository.observeAllDocuments().collect { list ->
                _uiState.update { it.copy(documents = list.groupBy { doc -> doc.medicalVisitId ?: "" }) }
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun addVisit(facility: String, doctor: String, questions: String, notes: String) {
        viewModelScope.launch {
            val now = Instant.now()
            val entity = MedicalVisitEntity(
                id = UUID.randomUUID().toString(),
                visitDate = LocalDate.now(),
                visitTime = null,
                facility = facility.ifBlank { null },
                doctor = doctor.ifBlank { null },
                preVisitQuestions = questions.ifBlank { null },
                postVisitNotes = notes.ifBlank { null },
                userConclusion = null,
                labelsJson = "[]",
                createdAt = now,
                updatedAt = now
            )
            medicalRecordsRepository.saveVisit(entity)
            _uiState.update { it.copy(showAddDialog = false) }
        }
    }

    fun deleteVisit(id: String) {
        viewModelScope.launch {
            // Delete associated documents first
            val docs = _uiState.value.documents[id] ?: emptyList()
            docs.forEach { doc ->
                medicalRecordsRepository.deleteDocument(doc.id)
                runCatching { File(doc.internalPath).delete() }
            }
            medicalRecordsRepository.deleteVisit(id)
        }
    }

    fun saveCapturedPhoto(visitId: String, file: File, displayName: String) {
        viewModelScope.launch {
            val doc = MedicalDocumentEntity(
                id = UUID.randomUUID().toString(),
                medicalVisitId = visitId,
                displayName = displayName,
                mimeType = "image/jpeg",
                internalPath = file.absolutePath,
                fileSizeBytes = file.length(),
                isEncrypted = false,
                createdAt = Instant.now()
            )
            medicalRecordsRepository.saveDocument(doc)
        }
    }

    fun deletePhoto(docId: String, filePath: String?) {
        viewModelScope.launch {
            medicalRecordsRepository.deleteDocument(docId)
            filePath?.let { runCatching { File(it).delete() } }
        }
    }

    fun setViewingPhoto(path: String?) {
        _uiState.update { it.copy(viewingPhotoPath = path) }
    }

    fun addMedication(name: String, dosage: String, frequency: String) {
        viewModelScope.launch {
            val now = Instant.now()
            val entity = MedicationScheduleEntity(
                id = UUID.randomUUID().toString(),
                medicationName = name,
                dosageNote = dosage.ifBlank { null },
                frequencyJson = """{"frequency":"$frequency"}""",
                startDate = LocalDate.now(),
                endDate = null,
                isActive = true,
                createdAt = now
            )
            medicalRecordsRepository.saveMedicationSchedule(entity)
            _uiState.update { it.copy(showAddMedicationDialog = false) }
        }
    }

    fun deactivateMedication(id: String) {
        viewModelScope.launch {
            medicalRecordsRepository.deactivateMedication(id)
        }
    }

    fun setAddDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showAddDialog = visible) }
    }

    fun setAddMedicationDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showAddMedicationDialog = visible) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalRecordsScreen(
    viewModel: MedicalRecordsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val formatter = remember { DateTimeFormatter.ofPattern("EEEE, 'ngày' d 'tháng' M, yyyy", Locale("vi")) }

    var facilityInput by remember { mutableStateOf("") }
    var doctorInput by remember { mutableStateOf("") }
    var questionsInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    var medNameInput by remember { mutableStateOf("") }
    var medDosageInput by remember { mutableStateOf("") }
    var medFrequencyInput by remember { mutableStateOf("Hàng ngày, sau ăn sáng") }

    val context = LocalContext.current
    var currentTargetVisitId by remember { mutableStateOf<String?>(null) }
    var tempCameraFile by remember { mutableStateOf<File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentTargetVisitId != null && tempCameraFile != null) {
            viewModel.saveCapturedPhoto(
                visitId = currentTargetVisitId!!,
                file = tempCameraFile!!,
                displayName = "Đơn thuốc / Kết quả khám (${LocalDate.now()})"
            )
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && currentTargetVisitId != null) {
            val visitId = currentTargetVisitId!!
            val dir = File(context.filesDir, "medical_docs").apply { mkdirs() }
            val destFile = File(dir, "doc_${visitId}_${System.currentTimeMillis()}.jpg")
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                viewModel.saveCapturedPhoto(visitId, destFile, "Ảnh kết quả / Đơn thuốc")
            }
        }
    }

    val launchCamera = { visitId: String ->
        currentTargetVisitId = visitId
        val dir = File(context.filesDir, "medical_docs").apply { mkdirs() }
        val file = File(dir, "doc_${visitId}_${System.currentTimeMillis()}.jpg")
        tempCameraFile = file
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        cameraLauncher.launch(uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hồ sơ y tế & Đơn thuốc", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (uiState.selectedTab == 0) {
                        viewModel.setAddDialogVisible(true)
                    } else {
                        viewModel.setAddMedicationDialogVisible(true)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (uiState.selectedTab == 0) "Thêm lượt khám" else "Thêm thuốc / vi chất"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = { Text("Lịch sử khám") },
                    icon = { Icon(Icons.Default.FolderShared, contentDescription = null) }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = { Text("Thuốc & Vi chất") },
                    icon = { Icon(Icons.Default.Medication, contentDescription = null) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.selectedTab == 0) {
                // TAB 0: LỊCH SỬ KHÁM
                if (uiState.visits.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FolderShared,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Chưa có lượt khám nào",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Nhấn + để lưu lại lịch hẹn, câu hỏi cho bác sĩ và kết luận.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(uiState.visits) { visit ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = visit.visitDate.format(formatter),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        IconButton(onClick = { viewModel.deleteVisit(visit.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }

                                    if (!visit.facility.isNullOrBlank()) {
                                        Text(
                                            text = "Cơ sở: ${visit.facility}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    if (!visit.doctor.isNullOrBlank()) {
                                        Text(
                                            text = "Bác sĩ: ${visit.doctor}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (!visit.preVisitQuestions.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Câu hỏi cần hỏi: ${visit.preVisitQuestions}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    if (!visit.postVisitNotes.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Kết luận & dặn dò: ${visit.postVisitNotes}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // PHOTO ATTACHMENTS (Camera / Gallery)
                                    val attachedDocs = uiState.documents[visit.id] ?: emptyList()
                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CameraAlt,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Ảnh đơn thuốc / KQ (${attachedDocs.size})",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            FilledTonalButton(
                                                onClick = { launchCamera(visit.id) },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Chụp", fontSize = 11.sp)
                                            }
                                            OutlinedButton(
                                                onClick = {
                                                    currentTargetVisitId = visit.id
                                                    galleryLauncher.launch("image/*")
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Chọn", fontSize = 11.sp)
                                            }
                                        }
                                    }

                                    if (attachedDocs.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            items(attachedDocs) { doc ->
                                                val bitmap = remember(doc.internalPath) {
                                                    runCatching {
                                                        val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
                                                        BitmapFactory.decodeFile(doc.internalPath, opts)
                                                    }.getOrNull()
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .size(86.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                                        .clickable { viewModel.setViewingPhoto(doc.internalPath) }
                                                ) {
                                                    if (bitmap != null) {
                                                        Image(
                                                            bitmap = bitmap.asImageBitmap(),
                                                            contentDescription = doc.displayName,
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    } else {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                                                        }
                                                    }

                                                    // Small delete badge
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.TopEnd)
                                                            .padding(3.dp)
                                                            .size(20.dp)
                                                            .clip(CircleShape)
                                                            .background(Color.Black.copy(alpha = 0.65f))
                                                            .clickable { viewModel.deletePhoto(doc.id, doc.internalPath) },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "Xóa ảnh",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // TAB 1: THUỐC & VI CHẤT (OFFLINE)
                if (uiState.medications.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Medication,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Chưa có danh sách thuốc hoặc vi chất",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ghi lại Sắt, Axit Folic, Canxi, Vitamin tổng hợp hoặc thuốc theo toa để theo dõi lịch uống.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(uiState.medications) { med ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (med.isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (med.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Medication,
                                            contentDescription = null,
                                            tint = if (med.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = med.medicationName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (!med.dosageNote.isNullOrBlank()) {
                                            Text(
                                                text = "Liều dùng: ${med.dosageNote}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Text(
                                            text = "Bắt đầu: ${med.startDate}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (med.isActive) {
                                        IconButton(
                                            onClick = { viewModel.deactivateMedication(med.id) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Hoàn thành đợt uống",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Visit Dialog
        if (uiState.showAddDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.setAddDialogVisible(false) },
                title = { Text("Thêm lượt khám mới") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = facilityInput,
                            onValueChange = { facilityInput = it },
                            label = { Text("Nơi khám (Bệnh viện / Phòng khám)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = doctorInput,
                            onValueChange = { doctorInput = it },
                            label = { Text("Bác sĩ phụ trách") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = questionsInput,
                            onValueChange = { questionsInput = it },
                            label = { Text("Câu hỏi muốn hỏi bác sĩ") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = notesInput,
                            onValueChange = { notesInput = it },
                            label = { Text("Dặn dò / Kết luận sau khám") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.addVisit(facilityInput, doctorInput, questionsInput, notesInput)
                            facilityInput = ""
                            doctorInput = ""
                            questionsInput = ""
                            notesInput = ""
                        }
                    ) {
                        Text("Lưu hồ sơ")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.setAddDialogVisible(false) }) {
                        Text("Hủy")
                    }
                }
            )
        }

        // Add Medication / Supplement Dialog
        if (uiState.showAddMedicationDialog) {
            val quickSuggestions = listOf("Viên sắt (Ferrous)", "Axit Folic 400mcg", "Canxi + D3", "Vitamin tổng hợp", "Dầu hoa anh thảo")

            AlertDialog(
                onDismissRequest = { viewModel.setAddMedicationDialogVisible(false) },
                title = { Text("Thêm thuốc hoặc vi chất") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Gợi ý vi chất phổ biến:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            quickSuggestions.take(3).forEach { suggestion ->
                                FilterChip(
                                    selected = medNameInput == suggestion,
                                    onClick = { medNameInput = suggestion },
                                    label = { Text(suggestion, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = medNameInput,
                            onValueChange = { medNameInput = it },
                            label = { Text("Tên thuốc / vi chất bổ sung *") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = medDosageInput,
                            onValueChange = { medDosageInput = it },
                            label = { Text("Liều lượng (VD: 1 viên, 500mg, 2 giọt...)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = medFrequencyInput,
                            onValueChange = { medFrequencyInput = it },
                            label = { Text("Tần suất / Thời điểm uống") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (medNameInput.isNotBlank()) {
                                viewModel.addMedication(medNameInput, medDosageInput, medFrequencyInput)
                                medNameInput = ""
                                medDosageInput = ""
                            }
                        },
                        enabled = medNameInput.isNotBlank()
                    ) {
                        Text("Thêm vào danh sách")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.setAddMedicationDialogVisible(false) }) {
                        Text("Hủy")
                    }
                }
            )
        }

        // Full-screen photo viewer dialog
        if (uiState.viewingPhotoPath != null) {
            val fullBitmap = remember(uiState.viewingPhotoPath) {
                runCatching {
                    BitmapFactory.decodeFile(uiState.viewingPhotoPath)
                }.getOrNull()
            }

            Dialog(
                onDismissRequest = { viewModel.setViewingPhoto(null) },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.94f))
                        .clickable { viewModel.setViewingPhoto(null) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ảnh đơn thuốc / Kết quả khám",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            IconButton(onClick = { viewModel.setViewingPhoto(null) }) {
                                Icon(Icons.Default.Close, contentDescription = "Đóng", tint = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        if (fullBitmap != null) {
                            Image(
                                bitmap = fullBitmap.asImageBitmap(),
                                contentDescription = "Xem ảnh kết quả khám",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = false)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text("Không thể tải hình ảnh", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
