package com.sos.commons.sign.keys.interfaces;

import java.io.IOException;

public interface StreamHandler {
	
	void handleStreamBuffer(byte[] buffer, int offset, int length) throws IOException;

}
