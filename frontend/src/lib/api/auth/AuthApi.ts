export const logout = async () => {
  try {
    const response = await fetch("http://localhost:8080/api/users/logout", {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
    });

    if (response.status === 204) {
      return;
    }

    if (!response.ok) {
      throw new Error(await response.text());
    }
  } catch (error) {
    throw error;
  }
};

// SMS 인증번호 발송 API
export const sendSmsVerification = async (phoneNumber: string) => {
  try {
    const response = await fetch("http://localhost:8080/api/sms/send", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ phoneNumber }),
    });

    const responseText = await response.text();
    let responseData;
    
    try {
      responseData = JSON.parse(responseText);
    } catch (e) {
      return { 
        isSuccess: false, 
        data: { 
          success: false, 
          message: responseText || "인증번호 발송에 실패했습니다." 
        } 
      };
    }
    
    if (response.ok && responseData.resultCode === 'OK') {
      return { 
        isSuccess: true, 
        data: responseData 
      };
    }
    
    let errorMessage = responseText;
    
    if (responseData) {
      if (responseData.data && responseData.data.message) {
        errorMessage = responseData.data.message;
      } else if (responseData.message) {
        errorMessage = responseData.message;
      } else if (responseData.msg) {
        errorMessage = responseData.msg;
      } else if (responseData.error) {
        errorMessage = responseData.error;
      }
    }
    
    if (!errorMessage || errorMessage === '{}') {
      errorMessage = "인증번호 발송에 실패했습니다.";
    }
    
    return { 
      isSuccess: false, 
      data: { 
        success: false, 
        message: errorMessage 
      } 
    };
  } catch (error: any) {
    return { 
      isSuccess: false, 
      data: { 
        success: false, 
        message: error?.message || "인증번호 발송 중 오류가 발생했습니다." 
      } 
    };
  }
};

// SMS 인증번호 확인 API
export const verifySmsCode = async (phoneNumber: string, code: string) => {
  try {
    const response = await fetch("http://localhost:8080/api/sms/verify", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ phoneNumber, code }),
    });

    if (response.ok) {
      return await response.json();
    }

    throw new Error(await response.text());
  } catch (error) {
    console.error("SMS 인증번호 확인 API 오류:", error);
    throw error;
  }
};

export const join = async (joinRequest: {
  email: string;
  name: string;
  phoneNumber: string;
  role: string;
  provider: string;
  oauthId: string;
  birthDate: string;
}) => {
  try {
    const response = await fetch("http://localhost:8080/api/users/join", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      credentials: "include",
      body: JSON.stringify(joinRequest),
    });

    if (response.ok) {
      const rsData = await response.json();
      return rsData;
    }

    throw new Error(await response.text());
  } catch (error) {
    console.error("회원가입 API 오류:", error);
    throw error;
  }
};
