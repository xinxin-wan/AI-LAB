// Copyright (c) 2022 Zhendong Peng (pzd17@tsinghua.org.cn)
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
#include <jni.h>

#include <string>
#include <thread>

#include "feature_pipeline.h"
#include "keyword_spotting.h"
#include "log.h"

namespace wekws {
    std::shared_ptr<KeywordSpotting> spotter;
    std::shared_ptr<wenet::FeaturePipelineConfig> feature_config;
    std::shared_ptr<wenet::FeaturePipeline> feature_pipeline;
    std::string result;  // NOLINT


    /**
     * 初始化 kws
     * @param env android context
     * @param jModelDir android app dir
     */
    void init(JNIEnv *env, jobject, jstring jModelDir) {
        const char *pModelDir = env->GetStringUTFChars(jModelDir, nullptr);

        std::string modelPath = std::string(pModelDir) + "/kws.ort";
        spotter = std::make_shared<KeywordSpotting>(modelPath);

        feature_config = std::make_shared<wenet::FeaturePipelineConfig>(40, 16000);
        feature_pipeline = std::make_shared<wenet::FeaturePipeline>(*feature_config);
    }

    /**
     * reset after stop spot, ready for next time start.
     * @param env android context
     */
    void reset(JNIEnv *env, jobject) {
        result = "";
        spotter->Reset();
        feature_pipeline->Reset();
    }

    /**
     * eat audio data
     * @param env
     * @param jWaveform
     */
    void accept_waveform(JNIEnv *env, jobject, jshortArray jWaveform) {
        jsize size = env->GetArrayLength(jWaveform);
        int16_t *waveform = env->GetShortArrayElements(jWaveform, 0);
        std::vector<int16_t> v(waveform, waveform + size);
        feature_pipeline->AcceptWaveform(v);
        env->ReleaseShortArrayElements(jWaveform, waveform, 0);

        LOG(INFO) << "kws accept waveform in ms: " << int(size / 16);
    }

    /**
     * set input finished, TODO: used for ?
     */
    void set_input_finished() {
        if (!feature_pipeline->input_finished()) {
            LOG(INFO) << "kws input finished";
            feature_pipeline->set_input_finished();
        } else {
            LOG(INFO) << "kws input already finished";
        }
    }

    /**
     * java layer thread kernel
     * @return real time prob
     */
    jfloat Decode() {
        std::vector<std::vector<float>> feats;
        if (feature_pipeline->Read(80, &feats)) {
            std::vector<std::vector<float>> prob;
            spotter->Forward(feats, &prob);
            jfloat max_prob = 0.0;
            for (int t = 0; t < prob.size(); t++) {
                for (int j = 0; j < prob[t].size(); j++) {
                    max_prob = std::max(prob[t][j], max_prob);
                }
            }
            return max_prob;
        } else {
            return 0;
        }
    }


}  // namespace wekws

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    jclass c = env->FindClass("com/intel/automotive/ai/phonon/kws/KwsJniStub");
    if (c == nullptr) {
        return JNI_ERR;
    }

    static const JNINativeMethod methods[] = {
            {"init",             "(Ljava/lang/String;)V", reinterpret_cast<void *>(wekws::init)},
            {"reset",            "()V",                   reinterpret_cast<void *>(wekws::reset)},
            {"acceptWaveform",   "([S)V",                 reinterpret_cast<void *>(wekws::accept_waveform)},
            {"setInputFinished", "()V",                   reinterpret_cast<void *>(wekws::set_input_finished)},
            {"Decode",           "()F",                   reinterpret_cast<void *>(wekws::Decode)},
    };
    int rc = env->RegisterNatives(c, methods, sizeof(methods) / sizeof(JNINativeMethod));

    if (rc != JNI_OK) {
        return rc;
    }

    return JNI_VERSION_1_6;
}
