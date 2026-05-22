package com.example.swift_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.swift_app.R;
import com.example.swift_app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button btnNext;
    private LinearLayout dotContainer;
    private OnboardingAdapter adapter;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        sessionManager = new SessionManager(this);
        viewPager = findViewById(R.id.onboardingPager);
        btnNext = findViewById(R.id.btnNext);
        dotContainer = findViewById(R.id.dotContainer);
        TextView btnSkip = findViewById(R.id.btnSkip);

        List<OnboardingPage> pages = new ArrayList<>();
        pages.add(new OnboardingPage("Zero-Fee Remittances", "Send money home instantly without any transaction fees.", android.R.drawable.ic_menu_send));
        pages.add(new OnboardingPage("Secure Digital Wallet", "Your money is protected by blockchain technology.", android.R.drawable.ic_lock_lock));
        pages.add(new OnboardingPage("Smart Financial Insights", "AI-powered recommendations help you save more.", android.R.drawable.ic_menu_info_details));

        adapter = new OnboardingAdapter(pages);
        viewPager.setAdapter(adapter);

        setupDots(pages.size());
        setCurrentDot(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setCurrentDot(position);
                if (position == adapter.getItemCount() - 1) {
                    btnNext.setText("Get Started");
                } else {
                    btnNext.setText("Next");
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() + 1 < adapter.getItemCount()) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                finishOnboarding();
            }
        });

        btnSkip.setOnClickListener(v -> finishOnboarding());
    }

    private void setupDots(int count) {
        ImageView[] dots = new ImageView[count];
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(8, 0, 8, 0);

        for (int i = 0; i < count; i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageDrawable(getResources().getDrawable(R.drawable.dot_inactive));
            dotContainer.addView(dots[i], params);
        }
    }

    private void setCurrentDot(int index) {
        int childCount = dotContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) dotContainer.getChildAt(i);
            if (i == index) {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.dot_active));
            } else {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.dot_inactive));
            }
        }
    }

    private void finishOnboarding() {
        sessionManager.setOnboardingDone(true);
        startActivity(new Intent(OnboardingActivity.this, LoginActivity.class));
        finish();
    }

    static class OnboardingPage {
        String title, description;
        int iconRes;
        OnboardingPage(String title, String description, int iconRes) {
            this.title = title; this.description = description; this.iconRes = iconRes;
        }
    }

    static class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.ViewHolder> {
        private final List<OnboardingPage> pages;
        OnboardingAdapter(List<OnboardingPage> pages) { this.pages = pages; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_onboarding_page, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            OnboardingPage page = pages.get(position);
            holder.title.setText(page.title);
            holder.desc.setText(page.description);
            holder.icon.setImageResource(page.iconRes);
        }

        @Override
        public int getItemCount() { return pages.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView title, desc;
            ImageView icon;
            ViewHolder(View view) {
                super(view);
                title = view.findViewById(R.id.onboardingTitle);
                desc = view.findViewById(R.id.onboardingDesc);
                icon = view.findViewById(R.id.onboardingIcon);
            }
        }
    }
}
