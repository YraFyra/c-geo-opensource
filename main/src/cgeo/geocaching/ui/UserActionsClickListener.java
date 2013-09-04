package cgeo.geocaching.ui;

import cgeo.geocaching.Geocache;

import android.view.View;
import android.widget.TextView;

/**
 * Listener for clicks on user name
 */
public class UserActionsClickListener extends AbstractUserClickListener {

    private final String name;

    public UserActionsClickListener(Geocache cache) {
        super(cache.supportsUserActions());
    }

    public UserActionsClickListener(String name) {
        super(true);
        this.name = name;
    }

    public UserActionsClickListener() {
        super(true);
    }

    @Override
    protected CharSequence getUserName(View view) {
        if (name != null) {
            return name;
        }
        return ((TextView) view).getText().toString();
    }
}

