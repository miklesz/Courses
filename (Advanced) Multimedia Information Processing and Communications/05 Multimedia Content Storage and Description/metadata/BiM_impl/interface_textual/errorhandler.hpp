#ifndef ERRORHANDLER
#define ERRORHANDLER

#include <sax/ErrorHandler.hpp>
#include <iostream.h>
#include <parsers/DOMParser.hpp>


class SAXParseException;


// ---------------------------------------------------------------------------
//  Simple error handler deriviative to install on parser
// ---------------------------------------------------------------------------
class DOMBiMErrorHandler : public ErrorHandler
{
public:
    // -----------------------------------------------------------------------
    //  Constructors and Destructor
    // -----------------------------------------------------------------------
    DOMBiMErrorHandler();
    ~DOMBiMErrorHandler();


    // -----------------------------------------------------------------------
    //  Getter methods
    // -----------------------------------------------------------------------
    bool getSawErrors() const;


    // -----------------------------------------------------------------------
    //  Implementation of the SAX ErrorHandler interface
    // -----------------------------------------------------------------------
    void warning(const SAXParseException& e);
    void error(const SAXParseException& e);
    void fatalError(const SAXParseException& e);
    void resetErrors();


private :
    // -----------------------------------------------------------------------
    //  Unimplemented constructors and operators
    // -----------------------------------------------------------------------
    DOMBiMErrorHandler(const DOMBiMErrorHandler&);
    void operator=(const DOMBiMErrorHandler&);


    // -----------------------------------------------------------------------
    //  Private data members
    //
    //  fSawErrors
    //      This is set if we get any errors, and is queryable via a getter
    //      method. Its used by the main code to suppress output if there are
    //      errors.
    // -----------------------------------------------------------------------
    bool    fSawErrors;
};


// ---------------------------------------------------------------------------
//  This is a simple class that lets us do easy (though not terribly efficient)
//  trancoding of XMLCh data to local code page for display.
// ---------------------------------------------------------------------------
class StrX
{
public :
    // -----------------------------------------------------------------------
    //  Constructors and Destructor
    // -----------------------------------------------------------------------
    StrX(const XMLCh* const toTranscode)
    {
        // Call the private transcoding method
        fLocalForm = XMLString::transcode(toTranscode);
    }

    ~StrX()
    {
        delete [] fLocalForm;
    }


    // -----------------------------------------------------------------------
    //  Getter methods
    // -----------------------------------------------------------------------
    const char* localForm() const
    {
        return fLocalForm;
    }

private :
    // -----------------------------------------------------------------------
    //  Private data members
    //
    //  fLocalForm
    //      This is the local code page form of the string.
    // -----------------------------------------------------------------------
    char*   fLocalForm;
};

inline ostream& operator<<(ostream& target, const StrX& toDump)
{
    target << toDump.localForm();
    return target;
}

inline bool DOMBiMErrorHandler::getSawErrors() const
{
    return fSawErrors;
}

#endif