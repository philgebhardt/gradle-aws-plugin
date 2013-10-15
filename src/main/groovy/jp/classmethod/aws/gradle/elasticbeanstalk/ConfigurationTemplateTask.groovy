package jp.classmethod.aws.gradle.elasticbeanstalk

import com.amazonaws.*
import com.amazonaws.services.elasticbeanstalk.*
import com.amazonaws.services.elasticbeanstalk.model.*

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction


class AWSElasticBeanstalkCreateConfigurationTemplateTask extends DefaultTask {
	
	{
		description 'Create/Migrate ElasticBeanstalk Configuration Templates.'
		group = 'AWS'
	}
	
	def String applicationName
	
	def String templateDescription = ''
	
	def Map<String, Closure<String>> configurationTemplates = [:]
	
	def solutionStackName = '64bit Amazon Linux running Tomcat 7'
	
	@TaskAction
	def createTemplate() {
		def AWSElasticBeanstalk eb = project.aws.eb
		
		configurationTemplates.each {
			def templateName = it.key
			def optionSettings = loadConfigurationOptions(it.value)
			
			try {
				eb.createConfigurationTemplate(new CreateConfigurationTemplateRequest()
					.withApplicationName(applicationName)
					.withTemplateName(templateName)
					.withDescription(templateDescription)
					.withSolutionStackName(solutionStackName)
					.withOptionSettings(optionSettings))
				println "configuration template $templateName @ $applicationName created"
			} catch (AmazonClientException e) {
				eb.updateConfigurationTemplate(new UpdateConfigurationTemplateRequest()
					.withApplicationName(applicationName)
					.withTemplateName(templateName)
					.withDescription(templateDescription)
					.withOptionSettings(optionSettings))
				// TODO withOptionsToRemove ?
				println "configuration template $templateName @ $applicationName updated"
			}
		}
	}
	
	def ConfigurationOptionSetting[] loadConfigurationOptions(Closure<String> jsonClosure) {
		def options = []
		new groovy.json.JsonSlurper().parseText(jsonClosure.call()).each {
			options += new ConfigurationOptionSetting(it.Namespace, it.OptionName, it.Value)
		}
		return options
	}
}

class AWSElasticBeanstalkDeleteConfigurationTemplateTask extends DefaultTask {
	
	{
		description 'Delete ElasticBeanstalk Configuration Templates.'
		group = 'AWS'
	}
	
	def String applicationName
	
	def String templateName
	
	@TaskAction
	def deleteTemplate() {
		def AWSElasticBeanstalk eb = project.aws.eb
		eb.deleteConfigurationTemplate(new DeleteConfigurationTemplateRequest()
			.withApplicationName(applicationName)
			.withTemplateName(templateName))
		println "configuration template $templateName @ $applicationName deleted"
	}
}
	