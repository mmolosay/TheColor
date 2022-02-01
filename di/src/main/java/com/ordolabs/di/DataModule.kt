package com.ordolabs.di

import dagger.Module

@Module(includes = [DataLocalModule::class, DataRemoteModule::class])
interface DataModule