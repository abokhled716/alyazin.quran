package com.alyazin.tafsir;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.alyazin.quran.R;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class TafsirActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TafsirAdapter adapter;
    private List<TafsirItem> tafsirList;
    private List<TafsirItem> filteredList = new ArrayList<>();
    private TextView surahNameTextView, ayahNumberTextView, noResultsTextView;
    private ImageButton searchButton;
    private EditText searchEditText;
    private boolean searchVisible = false;

    
    private LinkedHashMap<String, Integer> surahPositions = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tafsir);

        surahNameTextView = findViewById(R.id.surahName);
        ayahNumberTextView = findViewById(R.id.ayahNumber);
        searchButton = findViewById(R.id.searchButton);
        searchEditText = findViewById(R.id.searchEditText);
        noResultsTextView = findViewById(R.id.noResultsText);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tafsirList = TafsirLoader.load(this);
        filteredList.addAll(tafsirList);

        if (tafsirList != null && !tafsirList.isEmpty()) {
            prepareSurahPositions();
            updateToolbarInfo(filteredList.get(0));

            adapter = new TafsirAdapter(filteredList);
            recyclerView.setAdapter(adapter);

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                        if (firstVisibleItemPosition >= 0 && firstVisibleItemPosition < filteredList.size()) {
                            TafsirItem item = filteredList.get(firstVisibleItemPosition);
                            updateToolbarInfo(item);
                        }
                    }
                }
            });
        } else {
            Log.e("TafsirActivity", "فشل تحميل بيانات التفسير");
        }

        searchButton.setOnClickListener(v -> {
            searchVisible = !searchVisible;
            searchEditText.setVisibility(searchVisible ? View.VISIBLE : View.GONE);
            if (!searchVisible) {
                searchEditText.setText("");
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterResults(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // عند النقر على اسم السورة، نعرض قائمة منسدلة
        surahNameTextView.setOnClickListener(v -> showSurahDropdown());
    }

    private void prepareSurahPositions() {
        for (int i = 0; i < tafsirList.size(); i++) {
            String surah = tafsirList.get(i).surhr;
            if (!surahPositions.containsKey(surah)) {
                surahPositions.put(surah, i);
            }
        }
    }

    private void showSurahDropdown() {
        List<String> surahNames = new ArrayList<>(surahPositions.keySet());

        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, surahNames);
        spinner.setAdapter(adapter);

        new android.app.AlertDialog.Builder(this)
                .setTitle("اختر السورة")
                .setView(spinner)
                .setPositiveButton("انتقال", (dialog, which) -> {
                    String selectedSurah = (String) spinner.getSelectedItem();
                    Integer position = surahPositions.get(selectedSurah);
                    if (position != null) {
                        recyclerView.scrollToPosition(position);
                        Toast.makeText(this, "تم الانتقال إلى " + selectedSurah, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void filterResults(String query) {
        filteredList.clear();
        for (TafsirItem item : tafsirList) {
            if (item.ayah.contains(query) || item.tafsir.contains(query)) {
                filteredList.add(item);
            }
        }
        adapter.notifyDataSetChanged();

        noResultsTextView.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void updateToolbarInfo(TafsirItem item) {
        if (item != null) {
            surahNameTextView.setText("سورة " + item.surhr);
            ayahNumberTextView.setText("آية " + item.numberayah);
        }
    }
}
