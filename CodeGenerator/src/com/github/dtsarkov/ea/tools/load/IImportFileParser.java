package com.github.dtsarkov.ea.tools.load;

import java.util.ArrayList;

public interface IImportFileParser {
	ArrayList<ImportElement> execute(String fileName);
}
