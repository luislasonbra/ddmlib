/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.build.gradle.integration.application

import com.android.build.gradle.integration.common.fixture.GradleTestProject
import com.android.build.gradle.integration.common.fixture.app.HelloWorldApp
import com.android.builder.model.AndroidProject
import com.android.builder.model.ArtifactMetaData
import com.android.builder.model.JavaArtifact
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import static com.google.common.truth.Truth.assertThat
/**
 * Tests for the unit-tests related parts of the builder model.
 */
class UnitTestingModelTest {

    @Rule
    public GradleTestProject project = GradleTestProject.builder().create();

    @Before
    public void setUp() {
        new HelloWorldApp().writeSources(project.testDir)
        project.getBuildFile() << """
apply plugin: 'com.android.application'

android {
    compileSdkVersion $GradleTestProject.DEFAULT_COMPILE_SDK_VERSION
    buildToolsVersion "$GradleTestProject.DEFAULT_BUILD_TOOL_VERSION"
}
"""
    }

    @Test
    public void "Unit testing artifacts are included in the model"() {
        AndroidProject model = project.getSingleModel()

        assertThat(model.extraArtifacts*.name).containsExactly(
                AndroidProject.ARTIFACT_ANDROID_TEST,
                AndroidProject.ARTIFACT_UNIT_TEST)

        def unitTestMetadata =
                model.extraArtifacts.find { it.name == AndroidProject.ARTIFACT_UNIT_TEST }

        assert unitTestMetadata.isTest()
        assert unitTestMetadata.type == ArtifactMetaData.TYPE_JAVA

        for (variant in model.variants) {
            def unitTestArtifacts = variant.extraJavaArtifacts.findAll {
                it.name == AndroidProject.ARTIFACT_UNIT_TEST
            }
            assert unitTestArtifacts.size() == 1

            JavaArtifact unitTestArtifact = unitTestArtifacts.first()
            assert unitTestArtifact.name == AndroidProject.ARTIFACT_UNIT_TEST
            assertThat(unitTestArtifact.assembleTaskName).contains("UnitTest")
            assertThat(unitTestArtifact.assembleTaskName).contains(variant.name.capitalize())
            assertThat(unitTestArtifact.compileTaskName).contains("UnitTest")
            assertThat(unitTestArtifact.compileTaskName).contains(variant.name.capitalize())
        }
    }
}
