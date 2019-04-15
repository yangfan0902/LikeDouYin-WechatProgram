package com.imooc.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FetchRideoCover {
	private String ffmpegEXE;

	public FetchRideoCover(String ffmpegEXE) {
		super();
		this.ffmpegEXE = ffmpegEXE;
	}

	public void getCover(String videoInputPath, String coverOutputPath)
			throws Exception {
		// ffmpeg.exe -i 苏州大裤衩.mp4 -i bgm.mp3 -t 7 -y 新的视频.mp4
		List<String> command = new ArrayList<>();
		command.add(ffmpegEXE);
		// 指定截取第1秒
		command.add("-ss");
		command.add("00:00:01");

		command.add("-y");
		command.add("-i");
		command.add(videoInputPath);

		command.add("-vframes");
		command.add("1");

		command.add(coverOutputPath);

		// for (String c : command) {
		// System.out.print(c + " ");
		// }

		ProcessBuilder builder = new ProcessBuilder(command);
		Process process = builder.start();

		InputStream errorStream = process.getErrorStream();
		InputStreamReader inputStreamReader = new InputStreamReader(errorStream);
		BufferedReader br = new BufferedReader(inputStreamReader);

		String line = "";
		while ((line = br.readLine()) != null) {
		}

		if (br != null) {
			br.close();
		}
		if (inputStreamReader != null) {
			inputStreamReader.close();
		}
		if (errorStream != null) {
			errorStream.close();
		}

	}
}
