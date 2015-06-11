Overview
==============

feature-service intends to provide a common (across apps), easy-to-use feature switching service. It uses
Togglz (http://www.togglz.org/) as the underlying implementation. It is intended to be Spring-friendly, but also usable on
non-Spring apps.

If you are looking to do feature switching in a grails app, then grails-plugins/togz is a better option (although feature-service
should still work inside a grails application).

USAGE
==============
To use feature-service in an application, you will need to do 2 things:

1) Create an enumeration which implements org.togglz.core.Feature. Here is an example template:

public enum ApplicationFeature implements org.togglz.core.Feature {

    FEATURE_SWITCH_NAME;

    @Override
    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }
}

2) Create the FeatureConfiguration.

You must at least set the featureClass, which should be the enum that you created in step 1.
You also need to either specify an appName (e.g. "wotsync"), or a StateRepository implementation to use. The former is preferred,
since it will use the standard implementation.

Spring XML configuration example:

    <bean id="featureConfiguration" class="com.wotifgroup.featureservice.FeatureConfiguration">
        <property name="appName" value="wotsync"/>
        <property name="featureClass" value="com.wotif.supplier.feature.WotsyncFeature"/>
    </bean>

You can optionally also specify a UserProvider. This is only required if you want to control features by user, which for most
of our applications is not required.

If you are not using Spring, you will also need to call the FeatureConfiguration.init() method. This will bind the configuration into Togglz.
You should also call FeatureConfiguration.destroy() before your application exits.
NOTE: These methods are annotated with @PostConstruct and @PreDestroy, so for a Spring application explicit calling of these methods is not required.

You can also expose your features over JMX. To do so, use FeatureSwitchesMBean.
Spring XML configuration example:

    <bean id="featureSwitchesMBean" class="com.wotifgroup.featureservice.jmx.FeatureSwitchesMBean" depends-on="featureConfiguration"/>

You can optionally also implement SelfDescribing, which allows you to provide a description for each feature on the JMX page.


DropWizard Basic Auth Support
===================================

In addition to the step 2 described above, it is also possible to setup the Togglz console for Dropwizard based apps with
HTTP Basic Auth support. The following is an example template for the Dropwizard apps:

In your dropwizard initialize method, create a new FeatureServiceBundle and add it to bootstrap.
The only argument required on the constructor is your feature class as outlined in step 1 above.

    @Override
    public void initialize(Bootstrap<VirtualCreditCardConfiguration> bootstrap) {
        bootstrap.addBundle(new FeatureServiceBundle(ApplicationFeature.class))
    }

For the configuration, update your DropwizardConfiguration class as follows:

    MyConfiguration extends WotifConfiguration implements FeatureServiceConfiguration{

        @Valid
        @JsonProperty
        @NotNull
        private FeatureConfiguration feature = new FeatureConfiguration();

        FeatureConfiguration getFeatureConfiguration(){
            return feature
        }
    }

For your config.yml, it should look something like:

    feature:
      username: admin
      password: jaguarPurrs
      featureFilePath: /apps/wotifapps/myapp/config/features.properties
