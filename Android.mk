LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_PACKAGE_NAME := KernelAdiutor
LOCAL_PRIVILEGED_MODULE := true
LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_SRC_FILES := $(call all-java-files-under, app/src/main)
LOCAL_SRC_FILES += $(call all-java-files-under, ../FloatingActionButton/library/src/main)

LOCAL_RESOURCE_DIR := packages/apps/KernelAdiutor/app/src/main/res
LOCAL_RESOURCE_DIR += packages/apps/FloatingActionButton/library/src/main/res
LOCAL_RESOURCE_DIR += packages/apps/KernelAdiutor/res/appcompat
LOCAL_RESOURCE_DIR += packages/apps/KernelAdiutor/res/cardview

LOCAL_AAPT_INCLUDE_ALL_RESOURCES := true
LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += --extra-packages com.getbase.floatingactionbutton
LOCAL_AAPT_FLAGS += --extra-packages android.support.v7.appcompat
LOCAL_AAPT_FLAGS += --extra-packages android.support.v7.cardview
LOCAL_AAPT_FLAGS += --extra-packages android.support.v7.recyclerview
LOCAL_AAPT_FLAGS += --extra-packages android.support.annotations

LOCAL_ASSET_DIR := packages/apps/KernelAdiutor/app/src/main/assets

LOCAL_STATIC_JAVA_LIBRARIES += libdashclock libsupportv4 libcardviewv7 librecyclerviewv7 libappcompatv7 libannotations

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := libdashclock:libs/dashclock-api-r2.0.jar \
                                        libsupportv4:libs/android-support-v4.jar \
                                        libcardviewv7:libs/android-support-v7-cardview.jar \
                                        librecyclerviewv7:libs/android-support-v7-recyclerview.jar \
                                        libappcompatv7:libs/android-support-v7-appcompat.jar \
                                        libannotations:libs/android-support-annotations.jar

include $(BUILD_MULTI_PREBUILT)
