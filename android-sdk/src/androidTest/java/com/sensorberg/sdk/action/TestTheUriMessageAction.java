package com.sensorberg.sdk.action;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.os.Parcel;
import android.support.test.runner.AndroidJUnit4;

import java.util.UUID;

@RunWith(AndroidJUnit4.class)
public class TestTheUriMessageAction {

    private static final String MESSAGE = "message";
    private static final String TITLE = "title";
    private static final String URL = "http://www.sensorberg.com";
    UriMessageAction tested;

    @Before
    public void setUp() throws Exception {
        tested = new UriMessageAction(UUID.randomUUID(), MESSAGE, TITLE, URL, null, 0);
    }

    @Test
    public void test_parcelable(){
        Parcel output = Parcel.obtain();

        tested.writeToParcel(output, 0);
        output.setDataPosition(0);

        UriMessageAction copy = UriMessageAction.CREATOR.createFromParcel(output);

        Assertions.assertThat(copy.getUri()).isEqualTo(tested.getUri());
        Assertions.assertThat(copy.getTitle()).isEqualTo(tested.getTitle());
        Assertions.assertThat(copy.getContent()).isEqualTo(tested.getContent());

        Assertions.assertThat(copy.getType()).isEqualTo(ActionType.MESSAGE_URI);
    }
}
