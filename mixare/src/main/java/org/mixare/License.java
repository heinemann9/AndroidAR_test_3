package org.mixare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by MJ on 2016-11-12.
 */
public class License  extends Activity {

    ImageView logo;
    TextView info;
    TextView license1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.license);

        info = (TextView) findViewById(R.id.info);
        logo = (ImageView)findViewById(R.id.logo);
        license1 = (TextView) findViewById(R.id.license1);

        logo.setImageResource(R.drawable.logo);


        info.setText("α, β, γ GO\r\n" +
                "가천대학교 컴퓨터공학과 강신안\r\n" +
                "가천대학교 컴퓨터공학과 조민혁\r\n" +
                "가천대학교 컴퓨터공학과 차명진\r\n");
        license1.setText("Copyright (C) 2010- Peer internet solutions \n" +
                "\t\t\\n\\nThis program is free software: \tyou can redistribute it and/or modify it under the terms of the \n" +
                "\t\tGNU General Public License as published by the Free Software Foundation, either version 3 of the License, \n" +
                "\t\tor at your option) any later version. \n" +
                "\t\t\\n\\nThis program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even \n" +
                "\t\tthe implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public \n" +
                "\t\tLicense for more details. \n" +
                "\t\t\\n\\n You should have received a copy of the GNU General Public License along with this program. \n" +
                "\t\tIf not, see &lt;http://www.gnu.org/licenses/&gt;");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(License.this,MixView.class);
        startActivity(intent);
        finish();
    }
}
