package com.info.idol.community.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.Resource;
import com.google.gson.JsonArray;
import com.info.idol.community.Class.Board;
import com.info.idol.community.Class.BoardDetail;
import com.info.idol.community.Class.Comment;
import com.info.idol.community.Class.ResizeFactory;
import com.info.idol.community.GlobalApplication;
import com.info.idol.community.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BoardDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int ITEM_VIEW_TYPE_BODY = 0;
    public static final int ITEM_VIEW_TYPE_COMMENT = 1;
    public static final int ITEM_VIEW_TYPE_RECOMMENT = 2;
    private onLikeListener mOnLikeListener;
    private int selectedPosition = 0;
    private Boolean islike;
    private Context mContext;
    private ArrayList<BoardDetail> itemList;
    private SimpleDateFormat outputdateFormat = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());
    private SimpleDateFormat inputdateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public BoardDetailAdapter(Context context) {
        mContext = context;
        this.itemList = new ArrayList<>();
        islike = false;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_VIEW_TYPE_BODY) {
            return new BodyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_board_detail, parent, false));
        } else if (viewType == ITEM_VIEW_TYPE_COMMENT) {
            return new CommentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_board_comment, parent, false));
        } else {
            return new ChildrenCommentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_board_childrencomment, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {


        if (holder instanceof BodyViewHolder) {
            //아이템이 게시판글이라면 여기로옴.
            Board board = (Board) itemList.get(position).getData();
            if (board.getUser().getImage() != null) {
                Glide.with(mContext).load(mContext.getResources().getString(R.string.URL) + board.getUser().getImage()).centerCrop().into(((BodyViewHolder) holder).iv_user_image);
            } else {
                Glide.with(mContext).load(R.drawable.user).into(((BodyViewHolder) holder).iv_user_image);
            }
            ((BodyViewHolder) holder).tv_writer.setText(board.getUser().getNickname());
            String writeTime = "";
            try {
                Date date = inputdateFormat.parse(board.getDate());
                writeTime = outputdateFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            ((BodyViewHolder) holder).tv_write_time.setText(writeTime);
            ((BodyViewHolder) holder).tv_title.setText(board.getTitle());
            ((BodyViewHolder) holder).tv_body.setText(board.getBody());
            ((BodyViewHolder) holder).tv_like.setText(String.valueOf(board.getLike()));
            ((BodyViewHolder) holder).tv_like.setSelected(islike);
            ((BodyViewHolder) holder).tv_comment.setText(String.valueOf(board.getComment()));
            if (!board.getImage().isEmpty()) {
                try {
                    Log.e("ARRAY", "옴");
                    JSONArray json = new JSONArray(board.getImage());
                    for (int i = 0; i < json.length(); i++) {
                        Log.e("ARRAY", "" + json.get(i).toString());
                        ImageView imageView = new ImageView(mContext);
                        Glide.with(mContext).load(mContext.getResources().getString(R.string.URL) + json.get(i).toString()).fitCenter().into(imageView);
                        ((BodyViewHolder) holder).root.addView(imageView);

                        LinearLayout.LayoutParams plControl = (LinearLayout.LayoutParams) imageView.getLayoutParams();
                        int size = ResizeFactory.dpToPx(mContext, 20);
                        plControl.setMargins(size, size, size, size);
                        imageView.setLayoutParams(plControl);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else if (holder instanceof CommentViewHolder) {
            //선택된 댓글 표시하기 위한 조건
            if (selectedPosition == holder.getAdapterPosition()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ((CommentViewHolder) holder).root.setBackgroundColor(mContext.getResources().getColor(R.color.lightRed, null));
                } else {
                    ((CommentViewHolder) holder).root.setBackgroundColor(mContext.getResources().getColor(R.color.lightRed));
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.itemView.setBackground(mContext.getResources().getDrawable(R.drawable.topline, null));
                } else {
                    holder.itemView.setBackground(mContext.getResources().getDrawable(R.drawable.topline));

                }
            }

            Comment comment = (Comment) itemList.get(position).getData();
//            if (comment.getUser().getImage() != null) {
//                Glide.with(mContext).load(mContext.getResources().getString(R.string.URL) + comment.getUser().getImage()).centerCrop().into(((CommentViewHolder) holder).iv_user_image);
//            } else {
//                Glide.with(mContext).load(R.drawable.user).into(((CommentViewHolder) holder).iv_user_image);
//            }
//            ((CommentViewHolder) holder).tv_writer.setText(comment.getUser().getNickname());
//            try {
//                Date date = inputdateFormat.parse(comment.getDate());
//                writeTime = outputdateFormat.format(date);
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//            ((CommentViewHolder) holder).tv_write_time.setText(writeTime);
//            if (comment.getState() != 0) {
//                ((CommentViewHolder) holder).tv_body.setText("삭제된 댓글 입니다.");
//            } else {
//                ((CommentViewHolder) holder).tv_body.setText(comment.getContent());
//            }
            if (comment.getState() == 0) {
                //삭제하지 않은 댓글
                Glide.with(mContext).load(mContext.getResources().getString(R.string.URL) + comment.getUser().getImage()).centerCrop().error(R.drawable.user).into(((CommentViewHolder) holder).iv_user_image);
                ((CommentViewHolder) holder).tv_writer.setText(comment.getUser().getNickname());
                try {
                    Date date = inputdateFormat.parse(comment.getDate());
                    String writeTime = outputdateFormat.format(date);
                    ((CommentViewHolder) holder).tv_write_time.setText(writeTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                ((CommentViewHolder) holder).tv_body.setText(comment.getContent());
            } else {
                Glide.with(mContext).load(R.drawable.user).into(((CommentViewHolder) holder).iv_user_image);
                ((CommentViewHolder) holder).tv_writer.setText(R.string.delete_comment_name);
                ((CommentViewHolder) holder).tv_body.setText(R.string.delete_comment_body);
            }


        } else {
            Comment comment = (Comment) itemList.get(position).getData();
            if (comment.getUser().getImage() != null) {
                Glide.with(mContext).load(mContext.getResources().getString(R.string.URL) + comment.getUser().getImage()).centerCrop().into(((ChildrenCommentViewHolder) holder).iv_user_image);
            } else {
                Glide.with(mContext).load(R.drawable.user).into(((ChildrenCommentViewHolder) holder).iv_user_image);
            }
            ((ChildrenCommentViewHolder) holder).tv_writer.setText(comment.getUser().getNickname());
            String writeTime = "";
            try {
                Date date = inputdateFormat.parse(comment.getDate());
                writeTime = outputdateFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            ((ChildrenCommentViewHolder) holder).tv_write_time.setText(writeTime);
            ((ChildrenCommentViewHolder) holder).tv_body.setText(comment.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return itemList.get(position).getType();
    }

    public class BodyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public LinearLayout root;
        public TextView tv_writer;
        public TextView tv_write_time;
        public TextView tv_title;
        public TextView tv_body;
        public TextView tv_like;
        public TextView tv_comment;
        public ImageView iv_user_image;

        public BodyViewHolder(View itemView) {
            super(itemView);
            root = (LinearLayout) itemView.findViewById(R.id.ll_root);
            tv_writer = (TextView) itemView.findViewById(R.id.tv_writer);
            tv_write_time = (TextView) itemView.findViewById(R.id.tv_write_time);
            tv_title = (TextView) itemView.findViewById(R.id.tv_board_title);
            tv_body = (TextView) itemView.findViewById(R.id.tv_board_text);
            tv_like = (TextView) itemView.findViewById(R.id.tv_board_like);
            tv_comment = (TextView) itemView.findViewById(R.id.tv_board_comment);
            iv_user_image = (ImageView) itemView.findViewById(R.id.iv_user_image);
            tv_like.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            islike = !islike;
            view.setSelected(islike);
            int like = ((Board) itemList.get(0).getData()).getLike();
            if (islike) {
                like++;
            } else {
                like--;
            }
            ((Board) itemList.get(0).getData()).setLike(like);
            ((TextView) view).setText(String.valueOf(like));
            mOnLikeListener.onClick(islike);
        }
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout root;
        public TextView tv_writer;
        public TextView tv_write_time;
        public TextView tv_body;
        public ImageView iv_user_image;

        public CommentViewHolder(View itemView) {
            super(itemView);
            root = (LinearLayout) itemView.findViewById(R.id.root);
            tv_writer = (TextView) itemView.findViewById(R.id.tv_nickname);
            tv_write_time = (TextView) itemView.findViewById(R.id.tv_write_time);
            tv_body = (TextView) itemView.findViewById(R.id.tv_body);
            iv_user_image = (ImageView) itemView.findViewById(R.id.iv_user_image);


        }
    }

    static class ChildrenCommentViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_writer;
        public TextView tv_write_time;
        public TextView tv_body;
        public ImageView iv_user_image;

        public ChildrenCommentViewHolder(View itemView) {
            super(itemView);
            tv_writer = (TextView) itemView.findViewById(R.id.tv_nickname);
            tv_write_time = (TextView) itemView.findViewById(R.id.tv_write_time);
            tv_body = (TextView) itemView.findViewById(R.id.tv_body);
            iv_user_image = (ImageView) itemView.findViewById(R.id.iv_user_image);

        }
    }

    public void setIslike(Boolean islike) {
        this.islike = islike;
        notifyItemChanged(0);
    }

    public void setOnLikeListener(onLikeListener onLikeListener) {
        mOnLikeListener = onLikeListener;
    }

    public int addItem(BoardDetail item) {
        if (selectedPosition != 0) {
            for (int i = selectedPosition + 1; i < itemList.size(); i++) {
                Comment comment = (Comment) itemList.get(i).getData();
                if (comment.getParent() == null) {
                    itemList.add(i, item);
                    notifyItemInserted(i);
                    return i;
                }
            }
        }
        //여기는 부모댓글 또는 대댓글일경우는 마지막부모라 조건맞지 않아 나오는경우.
        itemList.add(item);
        notifyItemInserted(itemList.size() - 1);
        return itemList.size() - 1;
    }

    public void addItems(List<BoardDetail> items) {
        int start = itemList.size();
        int end = items.size();
        itemList.addAll(items);
        notifyItemRangeChanged(start, end);
    }

    public BoardDetail getItem(int position) {
        return itemList.get(position);
    }

    public void removeItem(int position) {
        itemList.remove(position);
        notifyItemRemoved(position);
    }

    public void changeComment(List<BoardDetail>items){
        BoardDetail body=itemList.get(0);
        itemList =new ArrayList<>();
        itemList.add(body);
        itemList.addAll(items);
        notifyDataSetChanged();
    }
    public void setSelectedPosition(int selectedPosition) {
        int preSelect = this.selectedPosition;
        this.selectedPosition = selectedPosition;
        if (preSelect != 0) {
            notifyItemChanged(preSelect);
        }
        if (selectedPosition != 0) {
            notifyItemChanged(selectedPosition);
        }
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public interface onLikeListener {
        void onClick(Boolean check);
    }
}
