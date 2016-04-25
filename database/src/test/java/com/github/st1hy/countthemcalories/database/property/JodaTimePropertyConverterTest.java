package com.github.st1hy.countthemcalories.database.property;

import com.github.st1hy.countthemcalories.database.BuildConfig;
import com.github.st1hy.countthemcalories.database.application.DatabaseApplication;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.EthiopicChronology;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, application = DatabaseApplication.class)
public class JodaTimePropertyConverterTest {
    private final JodaTimePropertyConverter jodaTimePropertyConverter = new JodaTimePropertyConverter();

    @Test
    public void testConversion() throws Exception {
        final DateTimeZone currentZone = DateTimeZone.forOffsetHours(2);
        final DateTime now = DateTime.now();

        Long jodaTimestamp = jodaTimePropertyConverter.convertToDatabaseValue(now
                .withZone(DateTimeZone.forOffsetHours(-23))
                .withChronology(EthiopicChronology.getInstance()));
        DateTime dateTime = jodaTimePropertyConverter.convertToEntityProperty(jodaTimestamp);
        Assert.assertNotNull(dateTime);
        Long jodaTimestamp2 = jodaTimePropertyConverter.convertToDatabaseValue(dateTime);
        assertThat(now.withZone(currentZone), equalTo(dateTime.withZone(currentZone)));
        assertThat(jodaTimestamp, equalTo(jodaTimestamp2));
    }

    @Test
    public void testNullValues() throws Exception {
        assertThat(jodaTimePropertyConverter.convertToDatabaseValue(null), nullValue());
        assertThat(jodaTimePropertyConverter.convertToEntityProperty(null), nullValue());

    }
}