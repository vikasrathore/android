package com.news.nytesttimes;

        import android.graphics.Bitmap;
        import android.support.v7.widget.RecyclerView;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageView;
        import android.widget.RelativeLayout;
        import android.widget.TextView;

        import com.news.nytesttimes.datatypes.News;

        import java.util.ArrayList;

/**
 * Created by vikasrathour on 18/09/15.
 */
public class PopularStoriesCardAdapter extends RecyclerView
        .Adapter<PopularStoriesCardAdapter
        .DataObjectHolder> {

    ArrayList<News> news;
    Bitmap defaultIcon;
    PopularFragment.OnFragmentInteractionListener listener;

    public PopularStoriesCardAdapter(ArrayList<News> news, Bitmap bitmap,PopularFragment.OnFragmentInteractionListener listener) {
        this.news = news;
        defaultIcon = bitmap;
        this.listener=listener;
    }

    public void setNews(ArrayList<News> news) {
        this.news = news;
    }

    public static class DataObjectHolder extends RecyclerView.ViewHolder{

        ImageView news_thumbnail;
        TextView news_short_desc;
        TextView news_headline;
        RelativeLayout parentLay;

        public DataObjectHolder(View itemView) {
            super(itemView);
            parentLay = (RelativeLayout) itemView.findViewById(R.id.newsItemTopLayout);
            news_headline = (TextView) parentLay.findViewById(R.id.news_headline);
            news_short_desc = (TextView) parentLay.findViewById(R.id.news_short_desc);
            news_thumbnail = (ImageView) parentLay.findViewById(R.id.news_thumb);

        }

    }

    @Override
    public PopularStoriesCardAdapter.DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.newsitem, parent, false);
        DataObjectHolder dataObjectHolder = new DataObjectHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //here you set your current position from holder of clicked view
                int mSelectedPosition = ((DataObjectHolder) view.getTag()).getPosition();

                 listener.onPopularFragmentInteraction(mSelectedPosition,news.get(mSelectedPosition).getNewsWebURL());

            }
        });
        view.setTag(dataObjectHolder);
        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(PopularStoriesCardAdapter.DataObjectHolder holder, int position) {


        holder.news_headline.setText(news.get(position).getSnippet());
        holder.news_short_desc.setText(news.get(position).getLead_paragraph());
        if (news.get(position).getLasTImageBitmap() == null)
            holder.news_thumbnail.setImageBitmap(defaultIcon);
        else
            holder.news_thumbnail.setImageBitmap(news.get(position).getLasTImageBitmap());

    }

    @Override
    public int getItemCount() {
        return news.size();
    }


}
