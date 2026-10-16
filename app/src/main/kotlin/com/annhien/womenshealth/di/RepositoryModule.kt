package com.annhien.womenshealth.di

import com.annhien.womenshealth.data.repository.*
import com.annhien.womenshealth.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCycleRepository(impl: CycleRepositoryImpl): CycleRepository

    @Binds
    @Singleton
    abstract fun bindJournalRepository(impl: JournalRepositoryImpl): JournalRepository

    @Binds
    @Singleton
    abstract fun bindDailyHealthRepository(impl: DailyHealthRepositoryImpl): DailyHealthRepository

    @Binds
    @Singleton
    abstract fun bindPregnancyRepository(impl: PregnancyRepositoryImpl): PregnancyRepository

    @Binds
    @Singleton
    abstract fun bindMedicalRecordsRepository(impl: MedicalRecordsRepositoryImpl): MedicalRecordsRepository

    @Binds
    @Singleton
    abstract fun bindKnowledgeRepository(impl: KnowledgeRepositoryImpl): KnowledgeRepository

    @Binds
    @Singleton
    abstract fun bindReminderRepository(impl: ReminderRepositoryImpl): ReminderRepository
}
