package com.example.project.archive;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.model.Page;

import java.util.ArrayList;
import java.util.List;

public class PagesDetailsAdapter extends RecyclerView.Adapter<PagesDetailsAdapter.MyView> {

    private List<Page> pages;
    private LayoutInflater mInflater;
    private int numPages = 0;

    public void setPages(ArrayList<Page> pages) {
        this.pages = pages;
    }

    public void setNumPages(int num) { numPages = num; }

    public PagesDetailsAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public class MyView extends RecyclerView.ViewHolder {

        ImageView image;
        EditText caption;
        RadioGroup rg;
        RadioButton[] btns = new RadioButton[5];

        public MyView(@NonNull View view) {
            super(view);
            image = view.findViewById(R.id.imageViewSinglePage);
            caption = view.findViewById(R.id.editTextCaption);
            rg = view.findViewById(R.id.radioGroupPages);
            // TODO fix
            btns[0] = view.findViewById(R.id.radio_button_1);
            btns[1] = view.findViewById(R.id.radio_button_2);
            btns[2] = view.findViewById(R.id.radio_button_3);
            btns[3] = view.findViewById(R.id.radio_button_4);
            btns[4] = view.findViewById(R.id.radio_button_5);

            caption.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {               }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {               }

                @Override
                public void afterTextChanged(Editable editable) {
                    Page page = pages.get(getAbsoluteAdapterPosition());
                    page.setCaption(editable.toString());
                    pages.set(getAbsoluteAdapterPosition(), page);
                }
            });

            rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    View radioButton = rg.findViewById(i);
                    int index = rg.indexOfChild(radioButton);
                    Page page = pages.get(getAbsoluteAdapterPosition());
                    page.setNum(index + 1);
                    pages.set(getAbsoluteAdapterPosition(), page);
                }
            });
        }
    }

    @NonNull
    @Override
    public PagesDetailsAdapter.MyView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.edit_pages_row_item,
                        parent,
                        false);

        return new PagesDetailsAdapter.MyView(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PagesDetailsAdapter.MyView holder, int position) {
        if (pages != null) {
            holder.image.setImageBitmap(pages.get(position).getImage());
            for (int i = numPages; i <= 4; ++i) {
                holder.btns[i].setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (pages != null) {
            return pages.size();
        } else {
            return 0;
        }
    }
}
