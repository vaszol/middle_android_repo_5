package com.yandex.practicum.middle_homework_5.gradle_plugins

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.gradle.api.GradleException
import org.w3c.dom.NodeList
import kotlin.io.path.listDirectoryEntries

abstract class FindUntranslatedStringsTask : DefaultTask() {
    @TaskAction
    fun findUntranslatedStrings() {
        val resDir = File(project.projectDir, "src/main/res")
        val strings = File(resDir, "values/strings.xml")

        //получить список всех строковых ресурсов из values/strings.xml,
        val stringsFromXml = getStrings(strings)
        val stringIdentities = getStringIdentities(stringsFromXml)
        //найти все каталоги с префиксами values-*,
        resDir.toPath().listDirectoryEntries("values-*")
            .forEach { it ->
                //для каждого каталога values-* получить список ресурсов,
                val valuesStrings = File(it.toFile(), "strings.xml")
                val stringsFromItXml = getStrings(valuesStrings)
                val stringList = getStringIdentities(stringsFromItXml)

                //если список ресурсов отличается, сгенерировать исключение GradleException.
                val missingStrings = stringIdentities.filter { !stringList.keys.contains(it.key) }
                if (missingStrings.isNotEmpty()) {
                    val stringBuilderErrorText =
                        StringBuilder("Missing translations").append(System.lineSeparator())
                            .append("$valuesStrings")
                            .append(System.lineSeparator())
                    missingStrings.forEach { missing ->
                        stringBuilderErrorText
                            .append("=== ${missing.key} = ${missing.value} ===")
                            .append(System.lineSeparator())
                    }
                    throw GradleException(stringBuilderErrorText.toString())
                }
            }
    }

    private fun getStrings(file: File) =
        DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(file)
            .getElementsByTagName("string")

    private fun getStringIdentities(stringsFromXml: NodeList): Map<String, String> =
        stringsFromXml.let { nodeList ->
            (0 until nodeList.length).map { i ->
                val node = nodeList.item(i) // node - один узел из списка stringsFromXml.
                val name = node.attributes?.getNamedItem("name")?.nodeValue ?: ""
                val value = node.firstChild.nodeValue ?: ""
                name to value
            }
        }.toMap()

}