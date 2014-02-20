package cgeo.contacts;

import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public final class ContactsActivity extends Activity implements DialogInterface.OnClickListener
{
    static final String LOG_TAG = "cgeo.contacts";

    private List<Pair<Integer, String>> contacts = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Uri uri = getIntent().getData();
        if (uri == null) {
            finish();
            return;
        }

        final String nickName = getParameter(uri, IContacts.PARAM_NAME);
        if (StringUtils.isEmpty(nickName)) {
            finish();
            return;
        }

        // search by nickname
        contacts = getContacts(nickName, ContactsContract.Data.CONTENT_URI, ContactsContract.Data.CONTACT_ID, ContactsContract.CommonDataKinds.Nickname.NAME);

        // search by display name
        if (contacts.size() == 0) {
            contacts = getContacts(null, Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "data/phones/filter/" + nickName), ContactsContract.Data.CONTACT_ID, ContactsContract.Contacts.DISPLAY_NAME);
        }

        if (contacts.size() == 0) {
            showToast(getString(R.string.contact_not_found, nickName));
            finish();
            return;
        } else if (contacts.size() == 1) {
            openContact(contacts.get(0));
        } else {
            List<String> list = new ArrayList<String>();
            for (Pair<Integer, String> p : contacts) {
                list.add(p.second);
            }
            final CharSequence[] items = list.toArray(new CharSequence[list.size()]);
            new AlertDialog.Builder(this)
                    .setTitle("Select user")
                    .setItems(items, this)
                    .create().show();
            return;
        }
        finish();
    }

    public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        openContact(contacts.get(which));
    }

    private void openContact(Pair<Integer, String> pair) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(pair.first));
        intent.setData(uri);
        startActivity(intent);
    }

    private List<Pair<Integer, String>> getContacts(final String searchName, Uri uri, final String idColumnName, final String selectionColumnName) {
        Integer foundId = 0;
        List<Pair<Integer, String>> list = new ArrayList<Pair<Integer, String>>();
        final String[] projection = new String[] { idColumnName, selectionColumnName };
        String selection = null;
        String[] selectionArgs = null;
        if (searchName != null) {
            selection = selectionColumnName + " LIKE ? COLLATE NOCASE";
            selectionArgs = new String[] { searchName };
        }
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, projection, selection, selectionArgs, null);
            while (cursor != null && !cursor.isLast() && cursor.moveToNext()) {
                foundId = Integer.valueOf(cursor.getInt(0));
                String foundName = cursor.getString(1);
                Log.d("geodaniel", "Found user " + foundName + " " + foundId);
                list.add(new Pair(foundId, foundName));
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "ContactsActivity.getContactId", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    public final void showToast(final String text) {
        final Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);

        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 100);
        toast.show();
    }

    private static String getParameter(final Uri uri, final String paramKey) {
        try {
            final String param = uri.getQueryParameter(paramKey);
            if (param == null) {
                return "";
            }
            return URLDecoder.decode(param, CharEncoding.UTF_8).trim();
        } catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG, "ContactsActivity.getParameter", e);
        }
        return "";
    }

}