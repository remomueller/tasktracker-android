import com.github.remomueller.tasktracker.android.DashboardActivity;
import com.github.remomueller.tasktracker.android.R;

import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class DashboardActivityTest {

    // private DashboardActivity activity;

    @Test
    public void shouldHaveCorrectAppName() throws Exception {
        String appName = new DashboardActivity().getResources().getString(R.string.app_name);
        assertThat(appName, equalTo("Task Tracker"));
    }

    // @Before
    //     public void setUp() {
    //         activity = new DashboardActivity();
    //         activity.onCreate(null);
    //     }

    // @Test
    //     public void itProperlyGreetsYou() {
    //         assertThat(textOf(id.loginSiteURL), equalTo("https://tasktracker.partners.org"));
    //     }

    // private String textOf(int id) {
    //     final TextView textView = (TextView)activity.findViewById(id);
    //     return textView.getText().toString();
    // }

}
