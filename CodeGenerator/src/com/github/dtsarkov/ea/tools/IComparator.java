package com.github.dtsarkov.ea.tools;

import com.github.dtsarkov.ea.tools.load.ImportElement;

public interface IComparator {
	boolean compare(Object eaElement, ImportElement importElement);
}
