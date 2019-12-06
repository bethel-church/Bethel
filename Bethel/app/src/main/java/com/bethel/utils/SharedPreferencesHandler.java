package com.bethel.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Base64InputStream;
import android.util.Base64OutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;


public class SharedPreferencesHandler {

	public static SharedPreferences getSharedPreferences(Context ctx) {
		return PreferenceManager.getDefaultSharedPreferences(ctx);
	}
	public static void clearObject(Context ctx, String key)
	{
		getSharedPreferences(ctx).edit().remove(key).commit();
	}
	public static void setStringValues(Context ctx, String key,
			String DataToSave) {
		Editor editor = getSharedPreferences(ctx).edit();
		editor.putString(key, DataToSave);
		editor.commit();
	}

	public static String getStringValues(Context ctx, String key) {
		return getSharedPreferences(ctx).getString(key, null);
	}

	public static void setIntValues(Context ctx, String key, int DataToSave) {
		Editor editor = getSharedPreferences(ctx).edit();
		editor.putInt(key, DataToSave);
		editor.commit();
	}

	public static int getIntValues(Context ctx, String key) {
		return getSharedPreferences(ctx).getInt(key, 0);
	}
	
	public static void setBooleanValues(Context ctx, String key, Boolean DataToSave) {
		Editor editor = getSharedPreferences(ctx).edit();
		editor.putBoolean(key, DataToSave);
		editor.commit();
	}

	public static boolean getBooleanValues(Context ctx, String key) {
		return getSharedPreferences(ctx).getBoolean(key, false);
	}
	
	public static long getLongValues(Context ctx, String key) {
		return getSharedPreferences(ctx).getLong(key, 0L);
	}
	
	public static void setLongValues(Context ctx, String key, Long DataToSave) {
		Editor editor = getSharedPreferences(ctx).edit();
		editor.putLong(key, DataToSave);
		editor.commit();
	}
	public static void writeObjectOnSharedPreference(Context ctx,String prefKey, Object object) {
		ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();

		ObjectOutputStream objectOutput;
		try {
			objectOutput = new ObjectOutputStream(arrayOutputStream);
			objectOutput.writeObject(object);
			byte[] data = arrayOutputStream.toByteArray();
			objectOutput.close();
			arrayOutputStream.close();

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Base64OutputStream b64 = new Base64OutputStream(out, Base64.DEFAULT);
			b64.write(data);
			b64.close();
			out.close();

			setStringValues(ctx,prefKey, new String(out.toByteArray()));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Object readObjectFromSharedPreference(Context ctx,String prefkey) {

		Object object = null;
		if(getSharedPreferences(ctx).contains(prefkey))
		{

			byte[] bytes = getStringValues(ctx,prefkey).getBytes();
			if (bytes.length == 0) {
				return null;
			}
			ByteArrayInputStream byteArray = new ByteArrayInputStream(bytes);
			Base64InputStream base64InputStream = new Base64InputStream(byteArray,
					Base64.DEFAULT);
			ObjectInputStream in;
			try {
				in = new ObjectInputStream(base64InputStream);

				object = in.readObject();
				in.close();
			} catch (StreamCorruptedException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			} catch (ClassNotFoundException e) {

				e.printStackTrace();
			}
		}
		return object;
	}
	
}