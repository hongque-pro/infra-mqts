package com.labijie.infra.mqts.spring.configuration

import com.labijie.infra.mqts.spring.annotation.EnableMqts
import org.springframework.context.annotation.ImportSelector
import org.springframework.core.type.AnnotationMetadata

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-10
 */
class MqtsRunnerSelector : ImportSelector {
    companion object{
        @JvmStatic
        internal var isImported: Boolean = false
        private set
    }

    override fun selectImports(metadata: AnnotationMetadata): Array<out String> {
        isImported = true
        val attributes = metadata.getAllAnnotationAttributes(EnableMqts::class.java.name)

        val autoStartService = (attributes?.getFirst("autoStartService") as? Boolean) ?: true

        val configurations = mutableListOf<String>()
        if (autoStartService) {
            configurations.add(MqtsRunnerAutoConfiguration::class.java.name)
        }

        return configurations.toTypedArray()
    }
}