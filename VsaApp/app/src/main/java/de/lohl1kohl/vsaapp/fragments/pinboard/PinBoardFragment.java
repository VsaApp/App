package de.lohl1kohl.vsaapp.fragments.pinboard;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import de.lohl1kohl.vsaapp.R;
import de.lohl1kohl.vsaapp.fragments.BaseFragment;
import de.lohl1kohl.vsaapp.loader.Callbacks;

import java.util.List;


public class PinBoardFragment extends BaseFragment {
    LinearLayout list;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_pinboard, container, false);
        list = root.findViewById(R.id.usersList);
        PinBoardUsersHolder.load(mActivity, true, () -> displayUsers(inflater));
        return root;
    }

    public void displayUsers(LayoutInflater inflater) {
        list.removeAllViews();
        final List<String> users = PinBoardUsersHolder.getUsers();
        for (String user : users) {
            View root = inflater.inflate(R.layout.pinboard_user_cell, null, false);
            TextView textView = root.findViewById(R.id.pinboard_user_cell_name);
            textView.setText(user);
            textView.setOnClickListener(view -> startActivity(new Intent(mActivity, PinBoardActivity.class).putExtra("user", user)));
            Button button = root.findViewById(R.id.pinboard_user_cell_follow);
            button.setText(PinBoardUsersHolder.isFollowing(mActivity, user) ? getString(R.string.following) : getString(R.string.follow));
            button.setTextColor(PinBoardUsersHolder.isFollowing(mActivity, user) ? getResources().getColor(R.color.colorPrimary) : Color.BLACK);
            button.setOnClickListener(view -> {
                Callbacks.baseCallback callback = new Callbacks.baseCallback() {
                    @Override
                    public void onReceived(String output) {
                        button.setText(PinBoardUsersHolder.isFollowing(mActivity, user) ? getString(R.string.following) : getString(R.string.follow));
                        button.setTextColor(PinBoardUsersHolder.isFollowing(mActivity, user) ? getResources().getColor(R.color.colorPrimary) : Color.BLACK);
                    }

                    @Override
                    public void onConnectionFailed() {
                        Toast.makeText(mActivity, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                    }
                };
                if (PinBoardUsersHolder.isFollowing(mActivity, user)) {
                    PinBoardUsersHolder.unfollow(mActivity, user, callback);
                } else {
                    PinBoardUsersHolder.follow(mActivity, user, callback);
                }
            });
            list.addView(root);
        }
        mActivity.runOnUiThread(() -> {
            try {
                list.getChildAt(list.getChildCount() - 1).findViewById(R.id.line).setVisibility(View.GONE);
            } catch (NullPointerException ignored) {

            }
        });
    }
}