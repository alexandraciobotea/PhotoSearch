package ro.upt.cs.photosearch;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder>{

    private ArrayList<MyPhoto> photoList;
    private Context context;

    public RecycleViewAdapter(Context context, ArrayList<MyPhoto> galleryList) {
        this.photoList = galleryList;
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView img;

        public ViewHolder(@NonNull View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            img = (ImageView) view.findViewById(R.id.img);
        }
    }

    @NonNull
    @Override
    public RecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context= viewGroup.getContext();
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_layout, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecycleViewAdapter.ViewHolder viewHolder, int i) {
        final MyPhoto p = photoList.get(i);
        viewHolder.img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        final String path = (photoList.get(i).cloud == false) ? "file://"+photoList.get(i).url : photoList.get(i).url;
        viewHolder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogLayout = inflater.inflate(R.layout.layout_dialog_photo, null);
                final ImageView image = dialogLayout.findViewById(R.id.dialogPhotoImg);
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(p.cloud == true) {
                            Toast.makeText(context,"Longitude is:"+p.longitude+"\nLatitude is: "+p.latitude,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                Picasso.with(context)
                        .load(path) // thumnail url goes here
//                        .resize(200,400)
                        //.centerCrop()
                        .fit()
                        .placeholder(R.mipmap.ic_launcher_round)
                        .error(R.mipmap.ic_launcher)
                        .into(image, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {

                            }
                        });
                AlertDialog dialog = null;
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        d.dismiss();
                    }
                });
                dialog = builder.create();

                dialog.setView(dialogLayout);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.show();


            }
        });

        Picasso.with(context)
                .load(path) // thumnail url goes here
                .resize(200,200)
                .centerCrop()
                .placeholder(R.drawable.blue_button_background)
                .error(R.mipmap.ic_launcher)
                .into(viewHolder.img, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                    }
                });

    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }
}
