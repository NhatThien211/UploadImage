package com.example.uploadimagetoportal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private static final int SELECT_IMAGE = 100;
    private Uri imageUri;
    private ImageView imageView;
    String imageName;

    // BƯỚC 1: đăng nhập azureportal -> tạo Storage account -> vô trong storage account vừa tạo ấn vào BLOBS
    //-> tạo folder chứa hình ảnh -> ấn vô folder vừa tạo sau đó chọn change access level qua "Container"
    // do bé Thiên đã config sẵn rồi nên ta bỏ qua bước 1 nha

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imgImage);
    }

    // BƯỚC 2 : tạo giao diện
    // Chọn hình ảnh từ điện thoại
    public void clickToChoose(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        // mở màn hình chọn hình ảnh
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);
    }

    // sau khi chọn hình ấn lưu cái thì sẽ vô onActivityResult và mang theo hình vừa chọn
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {
            case SELECT_IMAGE:
                if (resultCode == RESULT_OK) {
                    // láy link của hình trên máy
                    this.imageUri = imageReturnedIntent.getData();
                    // show hình ra cái imageview cho xem chơi
                    this.imageView.setImageURI(this.imageUri);
                }
        }
    }

    public void clickToUpload(View view) {
        UploadImage();
    }

    // BƯỚC 3: upload hình lên portal
    static final String CONNECTION_STRING = "DefaultEndpointsProtocol=https;AccountName=sqlvadtabpe45ilkho;AccountKey=Q0GtVfudYOKaYykP6CLCyk7uG/0Dak6C9WuAGDj5wQizMJDFEtEPaTGkGtdmNAatlbSXo4xznJAvOw4slPYAIg==;EndpointSuffix=core.windows.net";
    static final String IMAGE_FOLDER = "imagefolder";

    private void UploadImage() {
        try {
            final InputStream imageStream = getContentResolver().openInputStream(this.imageUri);
            final int imageLength = imageStream.available();
            final Handler handler = new Handler();
            Thread th = new Thread(new Runnable() {
                public void run() {
                    try {
                        // gọi thư viện người ta viết sẵn ImageManager
                        //gọi hàm upload và truyền tham số vào
                        // ta lên portal và ấn vô Access keys -> copy connection String
                        // lấy tên folder đã tạo ở bước một truyền vô
                        imageName = ImageManager.UploadImage(imageStream, imageLength, CONNECTION_STRING, IMAGE_FOLDER);
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(MainActivity.this, "Image Uploaded Successfully. Name = " + imageName, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception ex) {
                        final String exceptionMessage = ex.getMessage();
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(MainActivity.this, exceptionMessage, Toast.LENGTH_SHORT).show();
                                // ta xoá hình cũ khỏi image view
                                imageView.setImageURI(null);
                            }
                        });
                    }
                }
            });
            th.start();
        } catch (Exception ex) {

            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //BƯỚC 4: lấy hình đã up xuống
    // sau khi ta up hình lên thì chúng ta sẽ lấy link hình theo công thức [tên server] + [tên folder] + tên hình mới tạo ở trên
    //Hoặc ta nhìn link hình đã up thì sẽ lòi ra quy luật đặt tên
    final String serverName = "https://sqlvadtabpe45ilkho.blob.core.windows.net/";
    public void clickToGet(View view) {
        String imageURL = serverName+IMAGE_FOLDER+"/"+imageName;
        // ta dùng thư viện picasso để show hình từ link
        // ta add dependent vô grandle " implementation 'com.squareup.picasso:picasso:2.5.2' "
        // dùng picasso để load hình từ link về
        Picasso.with(this).load(imageURL).into(imageView);
    }
}
