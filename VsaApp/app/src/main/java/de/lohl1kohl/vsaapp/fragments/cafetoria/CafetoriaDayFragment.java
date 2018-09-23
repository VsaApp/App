package de.lohl1kohl.vsaapp.fragments.cafetoria;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Locale;

import de.lohl1kohl.vsaapp.R;
import de.lohl1kohl.vsaapp.fragments.BaseFragment;
import de.lohl1kohl.vsaapp.fragments.cafetoria.CafetoriaHolder.Day;
import de.lohl1kohl.vsaapp.fragments.cafetoria.CafetoriaHolder.Menu;

public class CafetoriaDayFragment extends BaseFragment {

    int day;

    public void setDay(int day) {
        this.day = day;
    }

    @SuppressLint({"ClickableViewAccessibility", "InflateParams", "SetTextI18n"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.cafetoria_day, container, false);

        new Thread(() -> {
            try {
                Day cafetoriaDay = CafetoriaHolder.getDay(day);
                String weekday = Arrays.asList(root.getResources().getStringArray(R.array.weekdays)).get(day);
                LayoutInflater layoutinflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                LinearLayout llMenus = root.findViewById(R.id.cafetoria_menus);
                mActivity.runOnUiThread(() -> llMenus.addView(createTopic(layoutinflater, getString(R.string.cafetoriaMenus))));
                mActivity.runOnUiThread(() -> llMenus.addView(createView(layoutinflater, cafetoriaDay.menu1)));
                mActivity.runOnUiThread(() -> llMenus.addView(createView(layoutinflater, cafetoriaDay.menu2)));

                LinearLayout llExtras = root.findViewById(R.id.cafetoria_extras);
                mActivity.runOnUiThread(() -> llExtras.addView(createTopic(layoutinflater, getString(R.string.cafetoriaExtra))));
                mActivity.runOnUiThread(() -> llExtras.addView(createView(layoutinflater, cafetoriaDay.extra)));

                if (!cafetoriaDay.snack.food.equals("")) {
                    LinearLayout llSnacks = root.findViewById(R.id.cafetoria_snacks);
                    mActivity.runOnUiThread(() -> llSnacks.addView(createTopic(layoutinflater, getString(R.string.cafetoriaSnacks))));
                    mActivity.runOnUiThread(() -> llSnacks.addView(createView(layoutinflater, cafetoriaDay.snack)));
                }

            } catch (IndexOutOfBoundsException ignored) {

            }
        }).start();


        return root;
    }

    @SuppressLint("ClickableViewAccessibility")
    private View createTopic(LayoutInflater layoutInflater, String topic) {
        // Create the view...
        TopicHolder topicHolder = new TopicHolder();
        View convertView = layoutInflater.inflate(R.layout.cafetoria_topic, null);
        topicHolder.topic = convertView.findViewById(R.id.cafetoria_topic);
        convertView.setTag(topicHolder);

        topicHolder.topic.setText(topic);

        return convertView;
    }

    @SuppressLint("ClickableViewAccessibility")
    private View createView(LayoutInflater layoutInflater, Menu menu) {
        // Create the view...
        ViewHolder topicHolder = new ViewHolder();
        View convertView = layoutInflater.inflate(R.layout.cafetoria_entry, null);
        topicHolder.menu = convertView.findViewById(R.id.cafetoria_menu);
        topicHolder.price = convertView.findViewById(R.id.cafetoria_price);
        topicHolder.time = convertView.findViewById(R.id.cafetoria_time);
        convertView.setTag(topicHolder);

        topicHolder.menu.setText(menu.food);
        topicHolder.price.setText(String.format(Locale.GERMAN, "%1$,.2f€", menu.price));
        if (!menu.startTime.equals("") && !menu.endTime.equals(""))
            topicHolder.time.setText(String.format(getString(R.string.cafetoriaTime), menu.startTime, menu.endTime));
        else if (!menu.startTime.equals("") && menu.endTime.equals(""))
            topicHolder.time.setText(String.format(getString(R.string.cafetoriaStartTime), menu.startTime));
        else {
            topicHolder.time.setVisibility(View.GONE);
        }

        return convertView;
    }

    static class ViewHolder {
        TextView menu;
        TextView price;
        TextView time;
    }

    static class TopicHolder {
        TextView topic;
    }

}
